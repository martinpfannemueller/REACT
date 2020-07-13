1. Download Eclipse for Java and DSL Developers from here: [http://www.eclipse.org/downloads/packages/](http://www.eclipse.org/downloads/packages/)
2. Clone MANTA Core and dummyproject
3. Put settings.xml and settings-security.xml to your .m2 folder (create if needed, on Windows create the folder using `.m2.` as name)
4. Install CPLEX
5. Install `Papyrus for UML` in eclipse using the according update site mentioned here: [https://www.eclipse.org/papyrus/download.html](https://www.eclipse.org/papyrus/download.html)
6. Open Eclipse and import the projects as existing Maven Projects
7. Run knowledge/Main.java (this is the knowledge repository/service)
8. Run testproject/ALRunner.java
9. This will crash: add CPLEX environment variable as described in exception to `<INSTALLFOLDER><...>/cplex/bin/x64_win64`