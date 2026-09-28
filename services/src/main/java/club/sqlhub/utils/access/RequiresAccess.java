package club.sqlhub.utils.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative access guard for controller methods.
 *
 * roles       — at least one must match the caller's role (hierarchy aware: SUPERADMIN > ADMIN > USER).
 * permissions — every entry must be present in the caller's module permissions.
 *               Format: "MODULE:OPERATION"  e.g. "SQL:WRITE"
 *
 * If roles is empty the role check is skipped.
 * If permissions is empty the permission check is skipped.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresAccess {
    String[] roles()       default {};
    String[] permissions() default {};
}
