This directory contains a workaround repository to host JARs that are not easily available for Maven. 

It follows the advice from...
http://stackoverflow.com/questions/364114/can-i-add-jars-to-maven-2-build-classpath-without-installing-them
...cited below:

...start of quote...
The static in-project repository solution
After putting this in your pom:

<repository>
    <id>repo</id>
    <releases>
        <enabled>true</enabled>
        <checksumPolicy>ignore</checksumPolicy>
    </releases>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
    <url>file://${project.basedir}/repo</url>
</repository>

for each artifact with a group id of form x.y.z Maven will include the following location inside your project dir in its search for artifacts:

repo/
| - x/
|   | - y/
|   |   | - z/
|   |   |   | - ${artifactId}/
|   |   |   |   | - ${version}/
|   |   |   |   |   | - ${artifactId}-${version}.jar

To elaborate more on this you can read this blog post.

Use Maven to install to project repo
Instead of creating this structure by hand I recommend to use a Maven plugin to install your jars as artifacts. So, to install an artifact to an in-project repository under repo folder execute:

mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=[your-jar] -DgroupId=[...] -DartifactId=[...] -Dversion=[...]If you'll choose this approach you'll be able to simplify the repository declaration in pom to:

<repository>
    <id>repo</id>
    <url>file://${project.basedir}/repo</url>
</repository>A helper script
Since executing installation command for each lib is kinda annoying and definitely error prone, I've created a utility script [https://github.com/nikita-volkov/install-to-project-repo] which automatically installs all the jars from a lib folder to a project repository, while automatically resolving all metadata (groupId, artifactId and etc.) from names of files. The script also prints out the dependencies xml for you to copy-paste in your pom.

Include the dependencies in your target package
When you'll have your in-project repository created you'll have solved a problem of distributing the dependencies of the project with its source, but since then your project's target artifact will depend on non-published jars, so when you'll install it to a repository it will have unresolvable dependencies. 

To beat this problem I suggest to include these dependencies in your target package. This you can do with either the Assembly Plugin or better with the OneJar Plugin. The official documentaion on OneJar is easy to grasp.

...end of quote...

This is combined with exist-db module naming advice and listing of JAR-files from https://code.google.com/p/atombeat/wiki/ExistMaven partially quoted below

...start of quote 2...
version=2.0-tech-preview

# manually install jars into local maven repository
mvn install:install-file -Dfile=$basedir/exist.jar -DgroupId=org.exist-db -DartifactId=exist -Dversion=$version -Dpackaging=jar
mvn install:install-file -Dfile=$basedir/exist-optional.jar -DgroupId=org.exist-db -DartifactId=exist-optional -Dversion=$version -Dpackaging=jar
mvn install:install-file -Dfile=$basedir/lib/core/xmldb.jar -DgroupId=org.exist-db -DartifactId=exist-xmldb -Dversion=$version -Dpackaging=jar
mvn install:install-file -Dfile=$basedir/lib/extensions/exist-versioning.jar -DgroupId=org.exist-db -DartifactId=exist-versioning -Dversion=$version -Dpackaging=jar
...end of quote 2...



		<dependency>
			<groupId>org.xmldb</groupId>
			<artifactId>xmldb-api</artifactId>
			<version>unknown</version>
		</dependency>

mvn install:install-file -Dfile=xmldb.jar -DgroupId=org.xmldb -DartifactId=xmldb-api -Dversion=unknown -Dpackaging=jar

