# Flyway Migration Policy

## Golden rule

- Do not modify an already applied `V__` migration in normal development.
- Treat `V1__create_core_tables.sql` and `V2__seed_initial_data.sql` as immutable history.

## What to do instead

- Add a new `V__` migration when schema changes are required.
- Update `R__sync_reference_data.sql` when roles, menus, demo users, or other reference data need to change.

## Why

- Editing an applied `V__` migration changes its checksum.
- Flyway then blocks startup with a validation error until `repair` is run.
- Repeatable migrations avoid this problem for mutable seed data.

## Practical examples

- Add a column: create `V3__add_xxx.sql`
- Change a menu name or seed role mapping: edit `R__sync_reference_data.sql`
- Add more demo prizes: edit `R__sync_reference_data.sql`

## Local repair

If a local test database already contains old checksums because a historical `V__` migration was edited, repair the
metadata instead of changing code around it:

```sql
update lottery_platform.flyway_schema_history
set checksum = <new_checksum>
where version = '<version>';
```

Or use the Flyway `repair` command for the same purpose.
