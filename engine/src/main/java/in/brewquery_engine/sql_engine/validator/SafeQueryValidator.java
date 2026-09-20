package in.brewquery_engine.sql_engine.validator;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.alter.Alter;

public class SafeQueryValidator {

    public static boolean validateQuery(String query, String type) {

        if (query == null)
            return false;

        query = query.trim();
        if (query.isEmpty())
            return false;

        if (query.contains(";") && query.indexOf(";") != query.length() - 1)
            return false;

        String sanitized = query.endsWith(";")
                ? query.substring(0, query.length() - 1).trim()
                : query;

        try {
            Statement stmt = CCJSqlParserUtil.parse(sanitized);

            type = type.trim().toUpperCase();

            switch (type) {
                case "SELECT":
                case "DQL":
                    return stmt instanceof Select;

                case "INSERT":
                case "DML_INSERT":
                    return stmt instanceof Insert;

                case "UPDATE":
                case "DML_UPDATE":
                    return stmt instanceof Update;

                case "DELETE":
                case "DML_DELETE":
                    return stmt instanceof Delete;

                case "DML":
                    return stmt instanceof Insert
                        || stmt instanceof Update
                        || stmt instanceof Delete;

                case "CREATE":
                case "DDL_CREATE":
                    return stmt instanceof CreateTable;

                case "DROP":
                case "DDL_DROP":
                    return stmt instanceof Drop;

                case "ALTER":
                case "DDL_ALTER":
                    return stmt instanceof Alter;

                case "DDL":
                    return stmt instanceof CreateTable
                        || stmt instanceof Drop
                        || stmt instanceof Alter;

                default:
                    return false;
            }

        } catch (Exception e) {
            return false;
        }
    }
}
