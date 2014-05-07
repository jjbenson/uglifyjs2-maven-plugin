uglifyjs2-maven-plugin
======================

Adds a number of run-time configuration options for the uglify step of a build process.

You can now do things like this:

<build>
	...
	<plugins>
		...
		<plugin>
			<groupId>org.codequarks</groupId>
			<artifactId>uglifyjs2-maven-plugin</artifactId>
			<version>0.0.2-SNAPSHOT</version>
			<executions>
			  ...
				<execution>
					<id>full-uglify-minify</id>
					<phase>process-classes</phase>
					<goals>
						<goal>uglify</goal>
					</goals>
					<configuration>
						<command>${uglify-command}</command>
						<options>-c -m</options>
						<merge>true</merge>
						<mainFileName>project-main.js</mainFileName>
						<sourceFiles>${project-source-files}</sourceFiles>
						<outputFileName>project.min.js</outputFileName>
						<jsSourceDir>${project.build.directory}/ugli</jsSourceDir>
						<jsOutputDir>${project.build.directory}/test-classes/js</jsOutputDir>
					</configuration>
				</execution>
				...
				<execution>
					<id>full-uglify-beautify</id>
					<phase>process-classes</phase>
					<goals>
						<goal>uglify</goal>
					</goals>
					<configuration>
						<command>${uglify-command}</command>
						<options>-b</options>
						<merge>true</merge>
						<mainFileName>project-main.js</mainFileName>
						<sourceFiles>${project-source-files}</sourceFiles>
						<outputFileName>project.js</outputFileName>
						<jsSourceDir>${project.build.directory}/ugli</jsSourceDir>
						<jsOutputDir>${project.build.directory}/test-classes/js</jsOutputDir>
					</configuration>
				</execution>
				...
