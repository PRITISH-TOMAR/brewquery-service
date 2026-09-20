"""
seed_movies.py — Inserts the Movies dataset into brew_hub (MongoDB Atlas).
Run: MONGO_URI=<uri> python scripts/seed_movies.py
     (or set MONGO_URI in your .env before running)
"""

import os
import sqlite3
import re as _re
from pymongo import MongoClient
from datetime import datetime, timezone

MONGO_URI   = os.environ["MONGO_URI"]
DB_NAME     = "brew_hub"
DATASET_ID  = "movies-dataset-001"

# ── Schema SQL ─────────────────────────────────────────────────────────────────
SCHEMA_SQL = """
CREATE TABLE genres (
  genre_id    INT PRIMARY KEY,
  name        VARCHAR(50) NOT NULL
);
CREATE TABLE directors (
  director_id INT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  birth_year  INT,
  nationality VARCHAR(50)
);
CREATE TABLE movies (
  movie_id        INT PRIMARY KEY,
  title           VARCHAR(200) NOT NULL,
  release_year    INT,
  director_id     INT REFERENCES directors(director_id),
  genre_id        INT REFERENCES genres(genre_id),
  runtime_minutes INT,
  budget          DECIMAL(15,2),
  revenue         DECIMAL(15,2),
  rating          DECIMAL(3,1),
  language        VARCHAR(50)
);
CREATE TABLE actors (
  actor_id    INT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  birth_year  INT,
  nationality VARCHAR(50)
);
CREATE TABLE movie_cast (
  movie_id       INT REFERENCES movies(movie_id),
  actor_id       INT REFERENCES actors(actor_id),
  character_name VARCHAR(100),
  billing_order  INT,
  PRIMARY KEY (movie_id, actor_id)
);
CREATE TABLE reviews (
  review_id   INT PRIMARY KEY,
  movie_id    INT REFERENCES movies(movie_id),
  user_name   VARCHAR(100),
  rating      DECIMAL(3,1),
  review_text TEXT,
  created_at  DATE
);
CREATE TABLE awards (
  award_id   INT PRIMARY KEY,
  movie_id   INT REFERENCES movies(movie_id),
  award_name VARCHAR(100),
  category   VARCHAR(100),
  year       INT,
  won        BOOLEAN
);
""".strip()

# ── Seed SQL Variant 1 — Hollywood blockbusters ────────────────────────────────
SEED_1 = """
INSERT INTO genres (genre_id, name) VALUES
(1,'Action'),(2,'Drama'),(3,'Comedy'),(4,'Thriller'),(5,'Sci-Fi');

INSERT INTO directors (director_id, name, birth_year, nationality) VALUES
(1,'Christopher Nolan',1970,'British'),
(2,'Martin Scorsese',1942,'American'),
(3,'Quentin Tarantino',1963,'American'),
(4,'Steven Spielberg',1946,'American');

INSERT INTO movies (movie_id, title, release_year, director_id, genre_id, runtime_minutes, budget, revenue, rating, language) VALUES
(1,'Inception',2010,1,5,148,160000000.00,836836967.00,8.8,'English'),
(2,'The Dark Knight',2008,1,1,152,185000000.00,1004558444.00,9.0,'English'),
(3,'Interstellar',2014,1,5,169,165000000.00,677471339.00,8.6,'English'),
(4,'Goodfellas',1990,2,2,146,25000000.00,46836394.00,8.7,'English'),
(5,'Pulp Fiction',1994,3,2,154,8000000.00,213928762.00,8.9,'English'),
(6,'Schindler''s List',1993,4,2,195,22000000.00,321306305.00,9.0,'English'),
(7,'The Hangover',2009,NULL,3,99,35000000.00,467483912.00,7.7,'English'),
(8,'The Prestige',2006,1,4,130,40000000.00,109676311.00,8.5,'English');

INSERT INTO actors (actor_id, name, birth_year, nationality) VALUES
(1,'Leonardo DiCaprio',1974,'American'),
(2,'Christian Bale',1974,'British'),
(3,'Matthew McConaughey',1969,'American'),
(4,'Robert De Niro',1943,'American'),
(5,'John Travolta',1954,'American'),
(6,'Liam Neeson',1952,'Irish');

INSERT INTO movie_cast (movie_id, actor_id, character_name, billing_order) VALUES
(1,1,'Cobb',1),(1,2,'Arthur',2),(2,2,'Bruce Wayne',1),
(3,3,'Cooper',1),(4,4,'Henry Hill',1),(5,5,'Vincent Vega',1),
(6,6,'Oskar Schindler',1),(8,2,'Alfred Borden',1);

INSERT INTO reviews (review_id, movie_id, user_name, rating, review_text, created_at) VALUES
(1,1,'user1',9.0,'Amazing mind-bending film','2023-01-15'),
(2,1,'user2',8.5,'Great concept and visuals','2023-02-10'),
(3,2,'user3',9.5,'Best superhero film ever','2023-01-20'),
(4,4,'user1',8.0,'Classic crime masterpiece','2023-03-05'),
(5,5,'user4',9.0,'Absolute masterpiece','2023-02-28');

INSERT INTO awards (award_id, movie_id, award_name, category, year, won) VALUES
(1,1,'Academy Award','Best Cinematography',2011,TRUE),
(2,2,'Academy Award','Best Sound Editing',2009,TRUE),
(3,4,'Academy Award','Best Picture',1991,FALSE),
(4,5,'Palme d''Or','Best Film',1994,TRUE),
(5,6,'Academy Award','Best Picture',1994,TRUE);
""".strip()

# ── Seed SQL Variant 2 — Contemporary cinema (edge cases: NULLs, sparse data) ─
SEED_2 = """
INSERT INTO genres (genre_id, name) VALUES
(1,'Action'),(2,'Drama'),(3,'Horror'),(4,'Romance');

INSERT INTO directors (director_id, name, birth_year, nationality) VALUES
(1,'David Fincher',1962,'American'),
(2,'Ridley Scott',1937,'British'),
(3,'Denis Villeneuve',1967,'Canadian');

INSERT INTO movies (movie_id, title, release_year, director_id, genre_id, runtime_minutes, budget, revenue, rating, language) VALUES
(1,'Fight Club',1999,1,2,139,63000000.00,100853753.00,8.8,'English'),
(2,'Se7en',1995,1,4,127,33000000.00,327311859.00,8.6,'English'),
(3,'Alien',1979,2,3,117,11000000.00,104931801.00,8.5,'English'),
(4,'Gladiator',2000,2,1,155,103000000.00,460583960.00,8.5,'English'),
(5,'Arrival',2016,3,2,116,47000000.00,203388186.00,7.9,'English'),
(6,'Dune',2021,3,1,155,165000000.00,401773200.00,8.0,'English'),
(7,'The Invisible Man',2020,NULL,3,124,7000000.00,143022667.00,7.1,'English');

INSERT INTO actors (actor_id, name, birth_year, nationality) VALUES
(1,'Brad Pitt',1963,'American'),
(2,'Edward Norton',1969,'American'),
(3,'Sigourney Weaver',1949,'American'),
(4,'Russell Crowe',1964,'Australian');

INSERT INTO movie_cast (movie_id, actor_id, character_name, billing_order) VALUES
(1,1,'Tyler Durden',1),(1,2,'The Narrator',2),
(2,1,'Detective Mills',1),(3,3,'Ellen Ripley',1),
(4,4,'Maximus',1),(6,1,'Duke Leto Atreides',2);

INSERT INTO reviews (review_id, movie_id, user_name, rating, review_text, created_at) VALUES
(1,1,'alice',8.5,'Mind-bending thriller','2023-01-15'),
(2,4,'bob',8.0,'Epic battles and drama','2023-03-10');

INSERT INTO awards (award_id, movie_id, award_name, category, year, won) VALUES
(1,3,'Saturn Award','Best Science Fiction Film',1980,TRUE),
(2,4,'Academy Award','Best Costume Design',2001,TRUE),
(3,1,'MTV Movie Award','Best Fight Scene',2000,FALSE);
""".strip()

# ── Seed SQL Variant 3 — World cinema classics (private, larger) ──────────────
SEED_3 = """
INSERT INTO genres (genre_id, name) VALUES
(1,'Action'),(2,'Drama'),(3,'Comedy'),(4,'Thriller'),(5,'Sci-Fi'),(6,'Animation');

INSERT INTO directors (director_id, name, birth_year, nationality) VALUES
(1,'Hayao Miyazaki',1941,'Japanese'),
(2,'Francis Ford Coppola',1939,'American'),
(3,'Stanley Kubrick',1928,'American'),
(4,'Alfred Hitchcock',1899,'British'),
(5,'Akira Kurosawa',1910,'Japanese');

INSERT INTO movies (movie_id, title, release_year, director_id, genre_id, runtime_minutes, budget, revenue, rating, language) VALUES
(1,'Spirited Away',2001,1,6,125,19000000.00,395802000.00,8.6,'Japanese'),
(2,'My Neighbor Totoro',1988,1,6,86,3700000.00,15000000.00,8.2,'Japanese'),
(3,'The Godfather',1972,2,2,175,6000000.00,245066411.00,9.2,'English'),
(4,'Apocalypse Now',1979,2,2,153,31500000.00,83471511.00,8.4,'English'),
(5,'2001: A Space Odyssey',1968,3,5,149,10500000.00,68900000.00,8.3,'English'),
(6,'The Shining',1980,3,4,146,19000000.00,44017374.00,8.4,'English'),
(7,'Psycho',1960,4,4,109,800000.00,50000000.00,8.5,'English'),
(8,'Rear Window',1954,4,4,112,1000000.00,37240000.00,8.5,'English'),
(9,'Seven Samurai',1954,5,1,207,500000.00,2700000.00,8.6,'Japanese'),
(10,'Rashomon',1950,5,2,88,250000.00,2000000.00,8.2,'Japanese');

INSERT INTO actors (actor_id, name, birth_year, nationality) VALUES
(1,'Marlon Brando',1924,'American'),
(2,'Al Pacino',1940,'American'),
(3,'Jack Nicholson',1937,'American'),
(4,'Anthony Perkins',1932,'American'),
(5,'James Stewart',1908,'American'),
(6,'Toshiro Mifune',1920,'Japanese'),
(7,'Masayuki Mori',1911,'Japanese'),
(8,'Martin Sheen',1940,'American');

INSERT INTO movie_cast (movie_id, actor_id, character_name, billing_order) VALUES
(3,1,'Vito Corleone',1),(3,2,'Michael Corleone',2),
(4,8,'Captain Willard',1),(6,3,'Jack Torrance',1),
(7,4,'Norman Bates',1),(8,5,'L.B. Jefferies',1),
(9,6,'Kambei Shimada',1),(10,6,'Tajomaru',1),(10,7,'Takehiro',2);

INSERT INTO reviews (review_id, movie_id, user_name, rating, review_text, created_at) VALUES
(1,3,'user1',9.5,'Greatest film ever made','2023-01-10'),
(2,3,'user2',9.0,'A timeless masterpiece','2023-02-15'),
(3,6,'user3',8.0,'Genuinely terrifying','2023-03-20'),
(4,7,'user4',8.5,'The original classic','2023-04-01'),
(5,9,'user5',9.0,'Epic samurai action','2023-05-10'),
(6,1,'user6',8.5,'Beautiful animation','2023-06-15'),
(7,2,'user7',8.0,'Heartwarming and gentle','2023-07-20'),
(8,5,'user8',7.5,'Philosophically profound','2023-08-25');

INSERT INTO awards (award_id, movie_id, award_name, category, year, won) VALUES
(1,1,'Academy Award','Best Animated Feature',2003,TRUE),
(2,3,'Academy Award','Best Picture',1973,TRUE),
(3,3,'Academy Award','Best Director',1973,FALSE),
(4,7,'BAFTA','Best British Film',1961,TRUE),
(5,9,'Venice Film Festival','Golden Lion',1954,TRUE),
(6,10,'Venice Film Festival','Golden Lion',1951,TRUE);
""".strip()

# ── Seed SQL parser ────────────────────────────────────────────────────────────
def _parse_value(v):
    """Coerce a raw SQL token to its Python type."""
    v = v.strip()
    if v.upper() == 'NULL':  return None
    if v.upper() == 'TRUE':  return True
    if v.upper() == 'FALSE': return False
    if v.startswith("'") and v.endswith("'"):
        return v[1:-1].replace("''", "'")
    try:
        return int(v)
    except ValueError:
        pass
    try:
        return float(v)
    except ValueError:
        return v

def _parse_row(row_str):
    """Parse a single comma-separated SQL value list, respecting quoted strings."""
    values, current, in_string, i = [], '', False, 0
    while i < len(row_str):
        c = row_str[i]
        if c == "'" and not in_string:
            in_string = True
            current += c
        elif c == "'" and in_string:
            if i + 1 < len(row_str) and row_str[i + 1] == "'":
                current += "''"
                i += 1
            else:
                in_string = False
                current += c
        elif c == ',' and not in_string:
            values.append(_parse_value(current.strip()))
            current = ''
        else:
            current += c
        i += 1
    if current.strip():
        values.append(_parse_value(current.strip()))
    return values

def parse_seed_to_tables(seed_sql):
    """
    Parse INSERT statements in seed_sql into structured table data.
    Returns: [{ "table": str, "columns": [...], "rows": [[...], ...] }]
    """
    import re
    tables = []
    for block in re.split(r'\n(?=INSERT INTO)', seed_sql, flags=re.IGNORECASE):
        m = re.match(
            r'INSERT INTO\s+(\w+)\s*\(([^)]+)\)\s*VALUES\s*([\s\S]+?);?\s*$',
            block.strip(), re.IGNORECASE
        )
        if not m:
            continue
        table_name = m.group(1)
        columns    = [c.strip() for c in m.group(2).split(',')]
        values_str = m.group(3)

        # State-machine extract of every (...) row group
        rows, depth, current_row = [], 0, ''
        for ch in values_str:
            if ch == '(':
                if depth == 0:
                    current_row = ''
                else:
                    current_row += ch
                depth += 1
            elif ch == ')':
                depth -= 1
                if depth == 0:
                    rows.append(_parse_row(current_row))
                else:
                    current_row += ch
            elif depth > 0:
                current_row += ch

        tables.append({"table": table_name, "columns": columns, "rows": rows})
    return tables

# Pre-compute sampleData for the two public seed variants (done once at seed time)
SAMPLE_DATA_1 = parse_seed_to_tables(SEED_1)
SAMPLE_DATA_2 = parse_seed_to_tables(SEED_2)

# ── SQLite runner: compute expected output for a TC ────────────────────────────
def _norm(v):
    """Normalize SQLite output: whole-number floats → int."""
    if isinstance(v, float) and v == int(v):
        return int(v)
    return v

def run_expected_sql(schema_sql, seed_sql, expected_sql):
    """
    Run expected_sql against an in-memory SQLite DB seeded with schema+seed.
    Returns { "columns", "rows", "rowsCount" } or None on error.
    """
    conn = sqlite3.connect(':memory:')
    try:
        for stmt in _re.split(r';\s*', schema_sql):
            s = stmt.strip()
            if s:
                conn.execute(s)
        for stmt in _re.split(r';\s*', seed_sql):
            s = stmt.strip()
            if s:
                conn.execute(s)
        conn.commit()
        cur = conn.execute(expected_sql)
        columns = [d[0] for d in cur.description]
        rows    = [[_norm(cell) for cell in row] for row in cur.fetchall()]
        return {"columns": columns, "rows": rows, "rowsCount": len(rows)}
    except Exception as e:
        print(f"  [SQLite warn] {e!r}")
        return None
    finally:
        conn.close()

# ── Helpers ────────────────────────────────────────────────────────────────────
def pad(n):
    return str(n).zfill(3)

def make_tc(q):
    qid = f"movies-q-{pad(q['idx'])}"
    tol = q.get("numericTolerance")

    # TC1 expected output — already hand-verified against SEED_1
    eo1 = {"columns": q["columns"], "rows": q["rows"], "rowsCount": len(q["rows"])}

    # TC2 expected output — computed via SQLite against SEED_2
    eo2 = run_expected_sql(SCHEMA_SQL, SEED_2, q["sql"])

    return {
        "_id": f"movies-tc-{pad(q['idx'])}",
        "questionId": qid,
        "type": "DQL",
        "expectedSql": q["sql"],
        "testCases": [
            {
                "_id": f"{qid}-tc1", "schemaSql": SCHEMA_SQL, "seedSql": SEED_1,
                "numericTolerance": tol, "type": "public",
                "sampleData": SAMPLE_DATA_1,
                "expectedOutput": eo1,
            },
            {
                "_id": f"{qid}-tc2", "schemaSql": SCHEMA_SQL, "seedSql": SEED_2,
                "numericTolerance": tol, "type": "public",
                "sampleData": SAMPLE_DATA_2,
                "expectedOutput": eo2,
            },
            {
                "_id": f"{qid}-tc3", "schemaSql": SCHEMA_SQL, "seedSql": SEED_3,
                "numericTolerance": tol, "type": "private",
                # no sampleData, no expectedOutput — private
            },
        ]
    }

def make_es(q):
    qid = f"movies-q-{pad(q['idx'])}"
    tol = q.get("numericTolerance")
    return {
        "_id": f"movies-es-{pad(q['idx'])}",
        "questionId": qid,
        "datasetId": DATASET_ID,
        "sqlMode": "postgresql",
        "solutions": [{
            "solutionQuery": q["sql"],
            "resultHash": f"PLACEHOLDER_movies_q{pad(q['idx'])}",
            "expectedOutput": {
                "columns": q["columns"],
                "rows": q["rows"],
                "rowsCount": len(q["rows"])
            }
        }],
        "createdAt": datetime.now(timezone.utc)
    }

# ── Dataset document ───────────────────────────────────────────────────────────
DATASET = {
    "_id": DATASET_ID,
    "slug": "movies",
    "title": "Movies Database",
    "description": (
        "Explore a rich movies database spanning genres, directors, actors, reviews, and awards. "
        "Practice SQL from basic filtering to advanced window functions and multi-table analytics."
    ),
    "icon": "🎬",
    "coverImage": "movies-cover.jpg",
    "tags": ["movies", "entertainment", "analytics", "joins", "window-functions"],
    "categories": ["Data Analysis", "Entertainment"],
    "skills": ["SELECT", "JOIN", "GROUP BY", "Subqueries", "Window Functions", "CTEs", "Aggregations"],
    "difficulty": "mixed",
    "sqlModesAvailable": ["postgresql", "mysql"],
    "questions": 50,
    "tableCount": 7,
    "dataType": "relational",
    "estimatedTime": "4–6 hours",
    "createdAt": datetime.now(timezone.utc)
}

# ── Metadata document ──────────────────────────────────────────────────────────
METADATA = {
    "_id": DATASET_ID,
    "tables": [
        {"name": "genres", "columns": [
            {"name": "genre_id", "type": "INT",         "primary": True,  "foreignKey": None},
            {"name": "name",     "type": "VARCHAR(50)", "primary": False, "foreignKey": None},
        ]},
        {"name": "directors", "columns": [
            {"name": "director_id", "type": "INT",          "primary": True,  "foreignKey": None},
            {"name": "name",        "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "birth_year",  "type": "INT",          "primary": False, "foreignKey": None},
            {"name": "nationality", "type": "VARCHAR(50)",  "primary": False, "foreignKey": None},
        ]},
        {"name": "movies", "columns": [
            {"name": "movie_id",        "type": "INT",           "primary": True,  "foreignKey": None},
            {"name": "title",           "type": "VARCHAR(200)",  "primary": False, "foreignKey": None},
            {"name": "release_year",    "type": "INT",           "primary": False, "foreignKey": None},
            {"name": "director_id",     "type": "INT",           "primary": False, "foreignKey": "directors.director_id"},
            {"name": "genre_id",        "type": "INT",           "primary": False, "foreignKey": "genres.genre_id"},
            {"name": "runtime_minutes", "type": "INT",           "primary": False, "foreignKey": None},
            {"name": "budget",          "type": "DECIMAL(15,2)", "primary": False, "foreignKey": None},
            {"name": "revenue",         "type": "DECIMAL(15,2)", "primary": False, "foreignKey": None},
            {"name": "rating",          "type": "DECIMAL(3,1)",  "primary": False, "foreignKey": None},
            {"name": "language",        "type": "VARCHAR(50)",   "primary": False, "foreignKey": None},
        ]},
        {"name": "actors", "columns": [
            {"name": "actor_id",    "type": "INT",          "primary": True,  "foreignKey": None},
            {"name": "name",        "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "birth_year",  "type": "INT",          "primary": False, "foreignKey": None},
            {"name": "nationality", "type": "VARCHAR(50)",  "primary": False, "foreignKey": None},
        ]},
        {"name": "movie_cast", "columns": [
            {"name": "movie_id",       "type": "INT",          "primary": True,  "foreignKey": "movies.movie_id"},
            {"name": "actor_id",       "type": "INT",          "primary": True,  "foreignKey": "actors.actor_id"},
            {"name": "character_name", "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "billing_order",  "type": "INT",          "primary": False, "foreignKey": None},
        ]},
        {"name": "reviews", "columns": [
            {"name": "review_id",   "type": "INT",          "primary": True,  "foreignKey": None},
            {"name": "movie_id",    "type": "INT",          "primary": False, "foreignKey": "movies.movie_id"},
            {"name": "user_name",   "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "rating",      "type": "DECIMAL(3,1)", "primary": False, "foreignKey": None},
            {"name": "review_text", "type": "TEXT",         "primary": False, "foreignKey": None},
            {"name": "created_at",  "type": "DATE",         "primary": False, "foreignKey": None},
        ]},
        {"name": "awards", "columns": [
            {"name": "award_id",   "type": "INT",          "primary": True,  "foreignKey": None},
            {"name": "movie_id",   "type": "INT",          "primary": False, "foreignKey": "movies.movie_id"},
            {"name": "award_name", "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "category",   "type": "VARCHAR(100)", "primary": False, "foreignKey": None},
            {"name": "year",       "type": "INT",          "primary": False, "foreignKey": None},
            {"name": "won",        "type": "BOOLEAN",      "primary": False, "foreignKey": None},
        ]},
    ]
}

# ── 50 Questions ───────────────────────────────────────────────────────────────
Q_LIST = [
    # ── EASY (1–20) ─────────────────────────────────────────────────────────
    {"idx":1,"title":"Movies by Release Year","difficulty":"easy","tags":["SELECT","ORDER BY"],
     "question":"List all movies showing only their title and release year, ordered from oldest to newest.",
     "sql":"SELECT title, release_year FROM movies ORDER BY release_year ASC",
     "columns":["title","release_year"],
     "rows":[["Goodfellas",1990],["Schindler's List",1993],["Pulp Fiction",1994],
             ["The Prestige",2006],["The Dark Knight",2008],["The Hangover",2009],
             ["Inception",2010],["Interstellar",2014]]},

    {"idx":2,"title":"Highly Rated Movies","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"Find all movies with a rating strictly greater than 8.0. Return all columns.",
     "sql":"SELECT * FROM movies WHERE rating > 8.0 ORDER BY rating DESC, title ASC",
     "columns":["movie_id","title","release_year","director_id","genre_id","runtime_minutes","budget","revenue","rating","language"],
     "rows":[[2,"The Dark Knight",2008,1,1,152,185000000,1004558444,9.0,"English"],
             [6,"Schindler's List",1993,4,2,195,22000000,321306305,9.0,"English"],
             [5,"Pulp Fiction",1994,3,2,154,8000000,213928762,8.9,"English"],
             [1,"Inception",2010,1,5,148,160000000,836836967,8.8,"English"],
             [4,"Goodfellas",1990,2,2,146,25000000,46836394,8.7,"English"],
             [3,"Interstellar",2014,1,5,169,165000000,677471339,8.6,"English"],
             [8,"The Prestige",2006,1,4,130,40000000,109676311,8.5,"English"]]},

    {"idx":3,"title":"Directors and Their Nationalities","difficulty":"easy","tags":["SELECT"],
     "question":"List the name and nationality of every director in the database, ordered by name.",
     "sql":"SELECT name, nationality FROM directors ORDER BY name ASC",
     "columns":["name","nationality"],
     "rows":[["Christopher Nolan","British"],["Martin Scorsese","American"],
             ["Quentin Tarantino","American"],["Steven Spielberg","American"]]},

    {"idx":4,"title":"Total Movie Count","difficulty":"easy","tags":["AGGREGATE","COUNT"],
     "question":"Count the total number of movies in the database.",
     "sql":"SELECT COUNT(*) AS total_movies FROM movies",
     "columns":["total_movies"],"rows":[[8]]},

    {"idx":5,"title":"Movies Released After 2000","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"Find all movies released after the year 2000. Show title and release year, ordered by year.",
     "sql":"SELECT title, release_year FROM movies WHERE release_year > 2000 ORDER BY release_year ASC",
     "columns":["title","release_year"],
     "rows":[["The Prestige",2006],["The Dark Knight",2008],["The Hangover",2009],
             ["Inception",2010],["Interstellar",2014]]},

    {"idx":6,"title":"Unique Languages","difficulty":"easy","tags":["SELECT","DISTINCT"],
     "question":"List all distinct languages used across movies in the database.",
     "sql":"SELECT DISTINCT language FROM movies ORDER BY language ASC",
     "columns":["language"],"rows":[["English"]]},

    {"idx":7,"title":"Action Movies","difficulty":"easy","tags":["JOIN","WHERE"],
     "question":"Find all movies that belong to the 'Action' genre. Return the movie title.",
     "sql":"SELECT m.title FROM movies m JOIN genres g ON m.genre_id = g.genre_id WHERE g.name = 'Action' ORDER BY m.title ASC",
     "columns":["title"],"rows":[["The Dark Knight"]]},

    {"idx":8,"title":"Short Movies","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"List movies with a runtime shorter than 100 minutes. Show title and runtime in minutes.",
     "sql":"SELECT title, runtime_minutes FROM movies WHERE runtime_minutes < 100 ORDER BY runtime_minutes ASC",
     "columns":["title","runtime_minutes"],"rows":[["The Hangover",99]]},

    {"idx":9,"title":"Younger Actors","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"Find all actors born after 1970. Return their name and birth year, ordered alphabetically.",
     "sql":"SELECT name, birth_year FROM actors WHERE birth_year > 1970 ORDER BY name ASC",
     "columns":["name","birth_year"],
     "rows":[["Christian Bale",1974],["Leonardo DiCaprio",1974]]},

    {"idx":10,"title":"Movies Per Genre","difficulty":"easy","tags":["AGGREGATE","GROUP BY","JOIN"],
     "question":"Count the number of movies in each genre. Show genre name and movie count, ordered by count descending.",
     "sql":"SELECT g.name AS genre, COUNT(m.movie_id) AS movie_count FROM genres g LEFT JOIN movies m ON g.genre_id = m.genre_id GROUP BY g.genre_id, g.name ORDER BY movie_count DESC, g.name ASC",
     "columns":["genre","movie_count"],
     "rows":[["Drama",3],["Sci-Fi",2],["Action",1],["Comedy",1],["Thriller",1]]},

    {"idx":11,"title":"Highest Rated Movie","difficulty":"easy","tags":["AGGREGATE","SUBQUERY"],
     "question":"Find the movie(s) with the highest rating. Return title and rating.",
     "sql":"SELECT title, rating FROM movies WHERE rating = (SELECT MAX(rating) FROM movies) ORDER BY title ASC",
     "columns":["title","rating"],
     "rows":[["Schindler's List",9.0],["The Dark Knight",9.0]]},

    {"idx":12,"title":"Movies by Budget","difficulty":"easy","tags":["SELECT","ORDER BY"],
     "question":"List all movies ordered by budget from highest to lowest. Show title and budget.",
     "sql":"SELECT title, budget FROM movies ORDER BY budget DESC",
     "columns":["title","budget"],
     "rows":[["The Dark Knight",185000000],["Interstellar",165000000],["Inception",160000000],
             ["The Prestige",40000000],["The Hangover",35000000],["Goodfellas",25000000],
             ["Schindler's List",22000000],["Pulp Fiction",8000000]]},

    {"idx":13,"title":"Loss-Making Movies","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"Find all movies where the production budget exceeded box office revenue. Return title, budget, and revenue.",
     "sql":"SELECT title, budget, revenue FROM movies WHERE budget > revenue ORDER BY title ASC",
     "columns":["title","budget","revenue"],"rows":[]},

    {"idx":14,"title":"Total Actor Count","difficulty":"easy","tags":["AGGREGATE","COUNT"],
     "question":"Return the total number of actors in the actors table.",
     "sql":"SELECT COUNT(*) AS total_actors FROM actors",
     "columns":["total_actors"],"rows":[[6]]},

    {"idx":15,"title":"Christopher Nolan Films","difficulty":"easy","tags":["JOIN","WHERE"],
     "question":"Find all movies directed by Christopher Nolan. Return the movie title ordered by release year.",
     "sql":"SELECT m.title FROM movies m JOIN directors d ON m.director_id = d.director_id WHERE d.name = 'Christopher Nolan' ORDER BY m.release_year ASC",
     "columns":["title"],
     "rows":[["The Prestige"],["The Dark Knight"],["Inception"],["Interstellar"]]},

    {"idx":16,"title":"All Genres","difficulty":"easy","tags":["SELECT","ORDER BY"],
     "question":"List all genres in alphabetical order.",
     "sql":"SELECT name FROM genres ORDER BY name ASC",
     "columns":["name"],
     "rows":[["Action"],["Comedy"],["Drama"],["Sci-Fi"],["Thriller"]]},

    {"idx":17,"title":"Mid-Range Rated Movies","difficulty":"easy","tags":["SELECT","WHERE","BETWEEN"],
     "question":"Find all movies with a rating between 7.0 and 8.5 inclusive. Return title and rating.",
     "sql":"SELECT title, rating FROM movies WHERE rating BETWEEN 7.0 AND 8.5 ORDER BY rating DESC",
     "columns":["title","rating"],
     "rows":[["The Prestige",8.5],["The Hangover",7.7]]},

    {"idx":18,"title":"Total Box Office Revenue","difficulty":"easy","tags":["AGGREGATE","SUM"],
     "question":"Calculate the total combined box office revenue of all movies.",
     "sql":"SELECT SUM(revenue) AS total_revenue FROM movies",
     "columns":["total_revenue"],"rows":[[3678098434.00]],"numericTolerance":0.01},

    {"idx":19,"title":"Veteran Directors","difficulty":"easy","tags":["SELECT","WHERE"],
     "question":"Find all directors born before 1960. Show name and birth year, ordered by birth year.",
     "sql":"SELECT name, birth_year FROM directors WHERE birth_year < 1960 ORDER BY birth_year ASC",
     "columns":["name","birth_year"],
     "rows":[["Martin Scorsese",1942],["Steven Spielberg",1946]]},

    {"idx":20,"title":"Award-Nominated Movies","difficulty":"easy","tags":["JOIN","DISTINCT"],
     "question":"List all distinct movie titles that appear in the awards table (whether won or not).",
     "sql":"SELECT DISTINCT m.title FROM movies m JOIN awards a ON m.movie_id = a.movie_id ORDER BY m.title ASC",
     "columns":["title"],
     "rows":[["Goodfellas"],["Inception"],["Pulp Fiction"],["Schindler's List"],["The Dark Knight"]]},

    # ── MEDIUM (21–35) ──────────────────────────────────────────────────────
    {"idx":21,"title":"Movies with Director Names","difficulty":"medium","tags":["LEFT JOIN"],
     "question":"List all movies along with their director's name. Include movies with no director (show NULL). Order by title.",
     "sql":"SELECT m.title, d.name AS director FROM movies m LEFT JOIN directors d ON m.director_id = d.director_id ORDER BY m.title ASC",
     "columns":["title","director"],
     "rows":[["Goodfellas","Martin Scorsese"],["Inception","Christopher Nolan"],
             ["Interstellar","Christopher Nolan"],["Pulp Fiction","Quentin Tarantino"],
             ["Schindler's List","Steven Spielberg"],["The Dark Knight","Christopher Nolan"],
             ["The Hangover",None],["The Prestige","Christopher Nolan"]]},

    {"idx":22,"title":"Average Rating per Genre","difficulty":"medium","tags":["AGGREGATE","GROUP BY","JOIN"],
     "question":"Calculate the average movie rating for each genre. Return genre name and average rating rounded to 2 decimal places, ordered by average rating descending.",
     "sql":"SELECT g.name AS genre, ROUND(AVG(m.rating), 2) AS avg_rating FROM genres g JOIN movies m ON g.genre_id = m.genre_id GROUP BY g.genre_id, g.name ORDER BY avg_rating DESC",
     "columns":["genre","avg_rating"],
     "rows":[["Action",9.0],["Drama",8.87],["Sci-Fi",8.7],["Thriller",8.5],["Comedy",7.7]],
     "numericTolerance":0.01},

    {"idx":23,"title":"Movie Details with Genre and Director","difficulty":"medium","tags":["INNER JOIN","MULTI-TABLE"],
     "question":"List movies with their genre name and director name. Only include movies that have both a genre and a director. Order by title.",
     "sql":"SELECT m.title, g.name AS genre, d.name AS director FROM movies m JOIN genres g ON m.genre_id = g.genre_id JOIN directors d ON m.director_id = d.director_id ORDER BY m.title ASC",
     "columns":["title","genre","director"],
     "rows":[["Goodfellas","Drama","Martin Scorsese"],["Inception","Sci-Fi","Christopher Nolan"],
             ["Interstellar","Sci-Fi","Christopher Nolan"],["Pulp Fiction","Drama","Quentin Tarantino"],
             ["Schindler's List","Drama","Steven Spielberg"],["The Dark Knight","Action","Christopher Nolan"],
             ["The Prestige","Thriller","Christopher Nolan"]]},

    {"idx":24,"title":"Top 5 Highest-Grossing Movies","difficulty":"medium","tags":["JOIN","ORDER BY","LIMIT"],
     "question":"Find the top 5 highest-grossing movies. Return title, genre name, and revenue.",
     "sql":"SELECT m.title, g.name AS genre, m.revenue FROM movies m JOIN genres g ON m.genre_id = g.genre_id ORDER BY m.revenue DESC LIMIT 5",
     "columns":["title","genre","revenue"],
     "rows":[["The Dark Knight","Action",1004558444],["Inception","Sci-Fi",836836967],
             ["Interstellar","Sci-Fi",677471339],["The Hangover","Comedy",467483912],
             ["Schindler's List","Drama",321306305]]},

    {"idx":25,"title":"Prolific Directors","difficulty":"medium","tags":["AGGREGATE","HAVING","GROUP BY"],
     "question":"Find directors who have directed more than 2 movies. Return name and movie count.",
     "sql":"SELECT d.name, COUNT(m.movie_id) AS movie_count FROM directors d JOIN movies m ON d.director_id = m.director_id GROUP BY d.director_id, d.name HAVING COUNT(m.movie_id) > 2 ORDER BY movie_count DESC",
     "columns":["name","movie_count"],
     "rows":[["Christopher Nolan",4]]},

    {"idx":26,"title":"Above-Average Rated Movies","difficulty":"medium","tags":["SUBQUERY","WHERE"],
     "question":"Find all movies with a rating above the overall average movie rating. Return title and rating.",
     "sql":"SELECT title, rating FROM movies WHERE rating > (SELECT AVG(rating) FROM movies) ORDER BY rating DESC, title ASC",
     "columns":["title","rating"],
     "rows":[["Schindler's List",9.0],["The Dark Knight",9.0],["Pulp Fiction",8.9],["Inception",8.8]]},

    {"idx":27,"title":"Multi-Film Actors","difficulty":"medium","tags":["AGGREGATE","HAVING","JOIN"],
     "question":"Find actors who have appeared in more than one movie. Return actor name and movie count.",
     "sql":"SELECT a.name, COUNT(mc.movie_id) AS movie_count FROM actors a JOIN movie_cast mc ON a.actor_id = mc.actor_id GROUP BY a.actor_id, a.name HAVING COUNT(mc.movie_id) > 1 ORDER BY movie_count DESC",
     "columns":["name","movie_count"],
     "rows":[["Christian Bale",3]]},

    {"idx":28,"title":"Top Revenue Genre","difficulty":"medium","tags":["AGGREGATE","GROUP BY","JOIN"],
     "question":"Find the genre with the highest total box office revenue. Return genre name and total revenue.",
     "sql":"SELECT g.name AS genre, SUM(m.revenue) AS total_revenue FROM genres g JOIN movies m ON g.genre_id = m.genre_id GROUP BY g.genre_id, g.name ORDER BY total_revenue DESC LIMIT 1",
     "columns":["genre","total_revenue"],"rows":[["Sci-Fi",1514308306.0]],"numericTolerance":0.01},

    {"idx":29,"title":"Review Count per Movie","difficulty":"medium","tags":["LEFT JOIN","AGGREGATE","GROUP BY"],
     "question":"Count the number of user reviews each movie has received. Include movies with zero reviews. Order by review count descending.",
     "sql":"SELECT m.title, COUNT(r.review_id) AS review_count FROM movies m LEFT JOIN reviews r ON m.movie_id = r.movie_id GROUP BY m.movie_id, m.title ORDER BY review_count DESC, m.title ASC",
     "columns":["title","review_count"],
     "rows":[["Inception",2],["The Dark Knight",1],["Goodfellas",1],["Pulp Fiction",1],
             ["Interstellar",0],["Schindler's List",0],["The Hangover",0],["The Prestige",0]]},

    {"idx":30,"title":"Unreviewed Movies","difficulty":"medium","tags":["LEFT JOIN","IS NULL"],
     "question":"Find all movies that have not received any user reviews. Return movie title.",
     "sql":"SELECT m.title FROM movies m LEFT JOIN reviews r ON m.movie_id = r.movie_id WHERE r.review_id IS NULL ORDER BY m.title ASC",
     "columns":["title"],
     "rows":[["Interstellar"],["Schindler's List"],["The Hangover"],["The Prestige"]]},

    {"idx":31,"title":"Cast Size per Movie","difficulty":"medium","tags":["LEFT JOIN","AGGREGATE","GROUP BY"],
     "question":"List each movie and the number of actors in its cast. Include movies with no cast. Order by cast size descending.",
     "sql":"SELECT m.title, COUNT(mc.actor_id) AS cast_count FROM movies m LEFT JOIN movie_cast mc ON m.movie_id = mc.movie_id GROUP BY m.movie_id, m.title ORDER BY cast_count DESC, m.title ASC",
     "columns":["title","cast_count"],
     "rows":[["Inception",2],["The Dark Knight",1],["Goodfellas",1],["Interstellar",1],
             ["Pulp Fiction",1],["Schindler's List",1],["The Prestige",1],["The Hangover",0]]},

    {"idx":32,"title":"Most Prolific Actor","difficulty":"medium","tags":["AGGREGATE","GROUP BY","LIMIT"],
     "question":"Find the actor who has appeared in the most movies. Return name and number of movies.",
     "sql":"SELECT a.name, COUNT(mc.movie_id) AS movie_count FROM actors a JOIN movie_cast mc ON a.actor_id = mc.actor_id GROUP BY a.actor_id, a.name ORDER BY movie_count DESC LIMIT 1",
     "columns":["name","movie_count"],"rows":[["Christian Bale",3]]},

    {"idx":33,"title":"Review vs Official Rating","difficulty":"medium","tags":["AGGREGATE","GROUP BY","JOIN"],
     "question":"For movies that have reviews, compare average user review rating to the official rating. Return title, official rating, and average review rating rounded to 2 decimal places.",
     "sql":"SELECT m.title, m.rating AS official_rating, ROUND(AVG(r.rating), 2) AS avg_review_rating FROM movies m JOIN reviews r ON m.movie_id = r.movie_id GROUP BY m.movie_id, m.title, m.rating ORDER BY m.title ASC",
     "columns":["title","official_rating","avg_review_rating"],
     "rows":[["Goodfellas",8.7,8.0],["Inception",8.8,8.75],["Pulp Fiction",8.9,9.0],["The Dark Knight",9.0,9.5]],
     "numericTolerance":0.01},

    {"idx":34,"title":"Movies from the Best Year","difficulty":"medium","tags":["SUBQUERY","IN"],
     "question":"Find all movies released in the same year(s) as the highest-rated movie. Return title and release year.",
     "sql":"SELECT title, release_year FROM movies WHERE release_year IN (SELECT release_year FROM movies WHERE rating = (SELECT MAX(rating) FROM movies)) ORDER BY title ASC",
     "columns":["title","release_year"],
     "rows":[["Schindler's List",1993],["The Dark Knight",2008]]},

    {"idx":35,"title":"Director Ranking by Average Rating","difficulty":"medium","tags":["AGGREGATE","GROUP BY","JOIN"],
     "question":"Rank directors by the average rating of their movies. Return director name and average rating rounded to 2 decimal places, ordered best first.",
     "sql":"SELECT d.name, ROUND(AVG(m.rating), 2) AS avg_rating FROM directors d JOIN movies m ON d.director_id = m.director_id GROUP BY d.director_id, d.name ORDER BY avg_rating DESC",
     "columns":["name","avg_rating"],
     "rows":[["Steven Spielberg",9.0],["Quentin Tarantino",8.9],["Christopher Nolan",8.73],["Martin Scorsese",8.7]],
     "numericTolerance":0.01},

    # ── HARD (36–50) ────────────────────────────────────────────────────────
    {"idx":36,"title":"Top 3 Movies per Genre","difficulty":"hard","tags":["WINDOW FUNCTION","RANK","PARTITION BY"],
     "question":"Using window functions, find the top 3 rated movies within each genre. Return genre name, movie title, rating, and rank. Order by genre then rank.",
     "sql":"""SELECT genre, title, rating, genre_rank
FROM (
  SELECT g.name AS genre, m.title, m.rating,
         RANK() OVER (PARTITION BY g.genre_id ORDER BY m.rating DESC) AS genre_rank
  FROM movies m JOIN genres g ON m.genre_id = g.genre_id
) ranked
WHERE genre_rank <= 3
ORDER BY genre ASC, genre_rank ASC""",
     "columns":["genre","title","rating","genre_rank"],
     "rows":[["Action","The Dark Knight",9.0,1],["Comedy","The Hangover",7.7,1],
             ["Drama","Schindler's List",9.0,1],["Drama","Pulp Fiction",8.9,2],["Drama","Goodfellas",8.7,3],
             ["Sci-Fi","Inception",8.8,1],["Sci-Fi","Interstellar",8.6,2],
             ["Thriller","The Prestige",8.5,1]]},

    {"idx":37,"title":"Cumulative Revenue Over Time","difficulty":"hard","tags":["WINDOW FUNCTION","SUM OVER","ORDER BY"],
     "question":"Calculate the running total of box office revenue ordered by release year. Return title, release year, revenue, and cumulative revenue. Exclude movies with NULL release year.",
     "sql":"""SELECT title, release_year, revenue,
       SUM(revenue) OVER (ORDER BY release_year, movie_id ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW) AS cumulative_revenue
FROM movies
WHERE release_year IS NOT NULL
ORDER BY release_year ASC, movie_id ASC""",
     "columns":["title","release_year","revenue","cumulative_revenue"],
     "rows":[["Goodfellas",1990,46836394,46836394],["Schindler's List",1993,321306305,368142699],
             ["Pulp Fiction",1994,213928762,582071461],["The Prestige",2006,109676311,691747772],
             ["The Dark Knight",2008,1004558444,1696306216],["Inception",2010,836836967,2533143183],
             ["Interstellar",2014,677471339,3210614522]],
     "numericTolerance":1},

    {"idx":38,"title":"Cross-Genre Actors","difficulty":"hard","tags":["JOIN","SUBQUERY","INTERSECT"],
     "question":"Find actors who have appeared in both an 'Action' movie and a 'Drama' movie. Return actor name.",
     "sql":"""SELECT DISTINCT a.name
FROM actors a
WHERE a.actor_id IN (
    SELECT mc.actor_id FROM movie_cast mc
    JOIN movies m ON mc.movie_id = m.movie_id
    JOIN genres g ON m.genre_id = g.genre_id WHERE g.name = 'Action'
)
AND a.actor_id IN (
    SELECT mc.actor_id FROM movie_cast mc
    JOIN movies m ON mc.movie_id = m.movie_id
    JOIN genres g ON m.genre_id = g.genre_id WHERE g.name = 'Drama'
)
ORDER BY a.name ASC""",
     "columns":["name"],"rows":[]},

    {"idx":39,"title":"Movies with Large Casts","difficulty":"hard","tags":["SUBQUERY","HAVING","AGGREGATE"],
     "question":"Find movies whose cast size is strictly above the average cast size across all movies. Return title and cast count.",
     "sql":"""SELECT m.title, COUNT(mc.actor_id) AS cast_count
FROM movies m JOIN movie_cast mc ON m.movie_id = mc.movie_id
GROUP BY m.movie_id, m.title
HAVING COUNT(mc.actor_id) > (
    SELECT AVG(cast_count) FROM (SELECT movie_id, COUNT(actor_id) AS cast_count FROM movie_cast GROUP BY movie_id) sub
)
ORDER BY cast_count DESC""",
     "columns":["title","cast_count"],"rows":[["Inception",2]]},

    {"idx":40,"title":"Directors with Varied Filmography","difficulty":"hard","tags":["AGGREGATE","HAVING","GROUP BY"],
     "question":"Find directors whose movies span a rating range of at least 0.5 (max minus min). Return director name, min rating, max rating, and range.",
     "sql":"""SELECT d.name, MIN(m.rating) AS min_rating, MAX(m.rating) AS max_rating,
       MAX(m.rating) - MIN(m.rating) AS rating_range
FROM directors d JOIN movies m ON d.director_id = m.director_id
GROUP BY d.director_id, d.name
HAVING MAX(m.rating) - MIN(m.rating) >= 0.5
ORDER BY rating_range DESC""",
     "columns":["name","min_rating","max_rating","rating_range"],
     "rows":[["Christopher Nolan",8.5,9.0,0.5]],"numericTolerance":0.01},

    {"idx":41,"title":"Revenue Share Within Genre","difficulty":"hard","tags":["WINDOW FUNCTION","SUM OVER PARTITION","ARITHMETIC"],
     "question":"For each movie, calculate its revenue as a percentage of its genre's total revenue. Return title, genre, revenue, and percentage rounded to 2 decimal places.",
     "sql":"""SELECT m.title, g.name AS genre, m.revenue,
       ROUND(m.revenue * 100.0 / SUM(m.revenue) OVER (PARTITION BY m.genre_id), 2) AS revenue_pct
FROM movies m JOIN genres g ON m.genre_id = g.genre_id
ORDER BY g.name ASC, revenue_pct DESC""",
     "columns":["title","genre","revenue","revenue_pct"],
     "rows":[["The Dark Knight","Action",1004558444,100.0],["The Hangover","Comedy",467483912,100.0],
             ["Schindler's List","Drama",321306305,55.2],["Pulp Fiction","Drama",213928762,36.73],
             ["Goodfellas","Drama",46836394,8.04],["Inception","Sci-Fi",836836967,55.26],
             ["Interstellar","Sci-Fi",677471339,44.74],["The Prestige","Thriller",109676311,100.0]],
     "numericTolerance":0.1},

    {"idx":42,"title":"Actors Without Award-Winning Credits","difficulty":"hard","tags":["SUBQUERY","NOT IN","JOIN"],
     "question":"Find actors who have never appeared in a movie that won an award. Return actor name.",
     "sql":"""SELECT a.name FROM actors a
WHERE a.actor_id NOT IN (
    SELECT DISTINCT mc.actor_id FROM movie_cast mc
    JOIN awards aw ON mc.movie_id = aw.movie_id
    WHERE aw.won = TRUE
)
ORDER BY a.name ASC""",
     "columns":["name"],"rows":[["Matthew McConaughey"],["Robert De Niro"]]},

    {"idx":43,"title":"Director Filmography Rankings","difficulty":"hard","tags":["WINDOW FUNCTION","RANK","PARTITION BY"],
     "question":"Rank each director's movies by rating within their own filmography (1 = best). Return director name, movie title, rating, and rank.",
     "sql":"""SELECT d.name AS director, m.title, m.rating,
       RANK() OVER (PARTITION BY m.director_id ORDER BY m.rating DESC) AS filmography_rank
FROM movies m JOIN directors d ON m.director_id = d.director_id
ORDER BY d.name ASC, filmography_rank ASC""",
     "columns":["director","title","rating","filmography_rank"],
     "rows":[["Christopher Nolan","The Dark Knight",9.0,1],["Christopher Nolan","Inception",8.8,2],
             ["Christopher Nolan","Interstellar",8.6,3],["Christopher Nolan","The Prestige",8.5,4],
             ["Martin Scorsese","Goodfellas",8.7,1],["Quentin Tarantino","Pulp Fiction",8.9,1],
             ["Steven Spielberg","Schindler's List",9.0,1]]},

    {"idx":44,"title":"Best Year for Movies","difficulty":"hard","tags":["AGGREGATE","GROUP BY","ORDER BY","LIMIT"],
     "question":"Find the release year with the highest average movie rating. Return year and average rating rounded to 2 decimal places.",
     "sql":"""SELECT release_year, ROUND(AVG(rating), 2) AS avg_rating
FROM movies WHERE release_year IS NOT NULL
GROUP BY release_year
ORDER BY avg_rating DESC, release_year ASC LIMIT 1""",
     "columns":["release_year","avg_rating"],"rows":[[1993,9.0]],"numericTolerance":0.01},

    {"idx":45,"title":"Actor Co-Star Network","difficulty":"hard","tags":["SELF-JOIN","COUNT DISTINCT","AGGREGATE"],
     "question":"For each actor, count how many unique co-stars they have appeared with. Return actor name and co-star count, ordered by count descending.",
     "sql":"""SELECT a.name, COUNT(DISTINCT mc2.actor_id) AS costar_count
FROM actors a
JOIN movie_cast mc1 ON a.actor_id = mc1.actor_id
JOIN movie_cast mc2 ON mc1.movie_id = mc2.movie_id AND mc1.actor_id <> mc2.actor_id
GROUP BY a.actor_id, a.name
ORDER BY costar_count DESC, a.name ASC""",
     "columns":["name","costar_count"],
     "rows":[["Christian Bale",1],["Leonardo DiCaprio",1]]},

    {"idx":46,"title":"Consistently High-Quality Genres","difficulty":"hard","tags":["AGGREGATE","HAVING","GROUP BY"],
     "question":"Find genres where every movie has a rating strictly above 8.5. Return genre name and its minimum rating.",
     "sql":"""SELECT g.name AS genre, MIN(m.rating) AS min_rating
FROM genres g JOIN movies m ON g.genre_id = m.genre_id
GROUP BY g.genre_id, g.name
HAVING MIN(m.rating) > 8.5
ORDER BY g.name ASC""",
     "columns":["genre","min_rating"],"rows":[["Action",9.0],["Drama",8.7]]},

    {"idx":47,"title":"Movie Profit Margins","difficulty":"hard","tags":["ARITHMETIC","ORDER BY"],
     "question":"Calculate the profit margin percentage for each movie as (revenue - budget) / budget * 100. Return title, budget, revenue, and profit margin rounded to 2 decimal places. Order by profit margin descending.",
     "sql":"""SELECT title, budget, revenue,
       ROUND((revenue - budget) * 100.0 / budget, 2) AS profit_margin_pct
FROM movies WHERE budget > 0
ORDER BY profit_margin_pct DESC""",
     "columns":["title","budget","revenue","profit_margin_pct"],
     "rows":[["Pulp Fiction",8000000,213928762,2574.11],["Schindler's List",22000000,321306305,1360.48],
             ["The Hangover",35000000,467483912,1235.67],["The Dark Knight",185000000,1004558444,442.99],
             ["Inception",160000000,836836967,423.02],["Interstellar",165000000,677471339,310.59],
             ["The Prestige",40000000,109676311,174.19],["Goodfellas",25000000,46836394,87.35]],
     "numericTolerance":0.1},

    {"idx":48,"title":"Frequent Co-Star Pairs","difficulty":"hard","tags":["SELF-JOIN","AGGREGATE","HAVING"],
     "question":"Find pairs of actors who have appeared together in more than one movie. Return both actor names and the number of shared movies.",
     "sql":"""SELECT a1.name AS actor1, a2.name AS actor2, COUNT(DISTINCT mc1.movie_id) AS shared_movies
FROM movie_cast mc1
JOIN movie_cast mc2 ON mc1.movie_id = mc2.movie_id AND mc1.actor_id < mc2.actor_id
JOIN actors a1 ON mc1.actor_id = a1.actor_id
JOIN actors a2 ON mc2.actor_id = a2.actor_id
GROUP BY mc1.actor_id, mc2.actor_id, a1.name, a2.name
HAVING COUNT(DISTINCT mc1.movie_id) > 1
ORDER BY shared_movies DESC, a1.name ASC""",
     "columns":["actor1","actor2","shared_movies"],"rows":[]},

    {"idx":49,"title":"Director Genre Diversity","difficulty":"hard","tags":["AGGREGATE","COUNT DISTINCT","JOIN"],
     "question":"Rank directors by how many distinct genres they have worked in. Return director name and genre count, ordered by diversity descending.",
     "sql":"""SELECT d.name, COUNT(DISTINCT m.genre_id) AS genre_diversity
FROM directors d JOIN movies m ON d.director_id = m.director_id
GROUP BY d.director_id, d.name
ORDER BY genre_diversity DESC, d.name ASC""",
     "columns":["name","genre_diversity"],
     "rows":[["Christopher Nolan",3],["Martin Scorsese",1],["Quentin Tarantino",1],["Steven Spielberg",1]]},

    {"idx":50,"title":"Biggest Rating Discrepancies","difficulty":"hard","tags":["AGGREGATE","ARITHMETIC","ABS","ORDER BY"],
     "question":"Find movies where the average user review rating differs most from the official rating. Return title, official rating, average review rating, and the absolute difference. Order by difference descending.",
     "sql":"""SELECT m.title, m.rating AS official_rating,
       ROUND(AVG(r.rating), 2) AS avg_review_rating,
       ROUND(ABS(m.rating - AVG(r.rating)), 2) AS rating_diff
FROM movies m JOIN reviews r ON m.movie_id = r.movie_id
GROUP BY m.movie_id, m.title, m.rating
ORDER BY rating_diff DESC, m.title ASC""",
     "columns":["title","official_rating","avg_review_rating","rating_diff"],
     "rows":[["Goodfellas",8.7,8.0,0.7],["The Dark Knight",9.0,9.5,0.5],
             ["Pulp Fiction",8.9,9.0,0.1],["Inception",8.8,8.75,0.05]],
     "numericTolerance":0.01},
]

# ── Build document arrays ──────────────────────────────────────────────────────
questions    = [{"_id": f"movies-q-{pad(q['idx'])}", "datasetId": DATASET_ID,
                 "title": q["title"], "question": q["question"],
                 "difficulty": q["difficulty"], "tags": q["tags"], "type": "DQL",
                 "createdAt": datetime.now(timezone.utc)} for q in Q_LIST]
testcases    = [make_tc(q) for q in Q_LIST]
solutions    = [make_es(q) for q in Q_LIST]

# ── Connect and seed ───────────────────────────────────────────────────────────
def main():
    print("\n🎬  Connecting to MongoDB Atlas (brew_hub)...")
    client = MongoClient(MONGO_URI, serverSelectionTimeoutMS=15000)
    db = client[DB_NAME]

    # Idempotent: clear existing movies data
    print("🗑   Removing existing Movies dataset data...")
    db.datasets.delete_one({"_id": DATASET_ID})
    db.metadata.delete_one({"_id": DATASET_ID})
    db.questions.delete_many({"datasetId": DATASET_ID})
    db.testcases.delete_many({"_id": {"$regex": "^movies-tc-"}})
    db.expected_solutions.delete_many({"datasetId": DATASET_ID})

    # Insert
    print("📥  Inserting dataset & metadata...")
    db.datasets.insert_one(DATASET)
    db.metadata.insert_one(METADATA)

    print(f"📥  Inserting {len(questions)} questions...")
    db.questions.insert_many(questions)

    print(f"📥  Inserting {len(testcases)} testcases (3 variants each)...")
    db.testcases.insert_many(testcases)

    print(f"📥  Inserting {len(solutions)} expected solutions...")
    db.expected_solutions.insert_many(solutions)

    # Verify
    q_count  = db.questions.count_documents({"datasetId": DATASET_ID})
    tc_count = db.testcases.count_documents({"_id": {"$regex": "^movies-tc-"}})
    es_count = db.expected_solutions.count_documents({"datasetId": DATASET_ID})

    print("\n✅  Seed complete!")
    print(f"    Dataset ID     : {DATASET_ID}")
    print(f"    Questions      : {q_count}")
    print(f"    Test cases     : {tc_count}  (public/public/private per question)")
    print(f"    Expected sols  : {es_count}")
    print(f"    Tables         : 7  (genres, directors, movies, actors, movie_cast, reviews, awards)")
    print("\n⚠   resultHash = PLACEHOLDER_movies_qXXX")
    print("    Run the judge engine solution executor to populate real hashes.\n")

    client.close()

if __name__ == "__main__":
    main()
