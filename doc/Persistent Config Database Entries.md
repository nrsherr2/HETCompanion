The SQLite database named `config` is designed as a key/value pair table to store config values that need to persist across app restarts. To keep everything simple, only Strings are supported. The list of what we store in the table is below.



| **Key** | **Description** | **Example Value** |
| --------- | ----------------- | ------------------- |
| `config_user_id` | User ID set in the settings page | `3` |
| `config_het_version` | Version of the HET Hardware device we are using | `42` |
