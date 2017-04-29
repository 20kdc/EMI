# EMI: Executable Modification Interface

This is meant for section manipulation and such needed to add/remove code to executables.

This can be used to build executables from scratch
by allocating the memory and putting assembled code inside,
or to add things to existing executables.

[ Fill in after GUI written and PE32 support ready. ]

Current Supported Formats:

MSDOS 'MZ' executables (quite minimal since they only have one section)
PE32 executables (Only tested i386 arch, some file-offset-relative-tables left unparsed)