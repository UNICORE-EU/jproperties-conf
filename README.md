# jproperties-conf

Lightweight library providing a comprehensive configuration framework based on Java Properties format.

Used as follows: 
1. expected contents of configuration is declared in code
2. library parses the given properties config, ensuring validity of configuration and allowing for more convenient 
access to settings.

Most important features:
* mandatory options
* defining default values
* non-string value types, like numbers or file paths
* list of items
* structural lists: a list of properties, where each list entry can have multiple well-defined values
* generating table in Asciidoc format with documentation of configuration derived from Java code.

## Historical note

This library was previously part of UNICORE eu.unicore.security:securityLibrary module (up to its version 4.x inclusive). 
It was moved to a separate repository and maven module, preserving source code package but the group and artifact id
were changed to the current ones.
