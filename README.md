uglifyjs2-maven-plugin
======================

Adds a number of run-time configuration options for the uglify step of a build process.

You can now do things like this:

	<properties>
		...
		<uglify-command>/home/user/node_modules/uglify-js/bin/uglifyjs</uglify-command>
		<project-source-files>
		    a1.js,
		    a2.js,
		    b.js,
		    d.js,
		    c.js,
		    i.js,
		    l.js
		    ...
		    x.js
		</project-source-files>
	</properties>
		...
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
								<jsSourceDir>${project.build.directory}/ugli-source</jsSourceDir>
								<jsOutputDir>${project.build.directory}/ugli-destination</jsOutputDir>
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
								<jsSourceDir>${project.build.directory}/ugli-source</jsSourceDir>
								<jsOutputDir>${project.build.directory}/ugli-destination</jsOutputDir>
							</configuration>
						</execution>
						...