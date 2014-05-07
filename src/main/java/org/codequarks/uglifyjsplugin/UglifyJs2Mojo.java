package org.codequarks.uglifyjsplugin;

/*
 * Copyright 2013-2014 João Lemos https://github.com/joaolemos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 * 
 * @goal uglify
 * 
 * @phase process-classes
 */
public class UglifyJs2Mojo extends AbstractMojo {

	/**
	 * uglifyjs Command to execute
	 * 
	 * @parameter property="uglifyjs"
	 */
	private String command = "uglifyjs";

	/**
	 * uglifyjs Command options
	 * 
	 * @parameter property="options"
	 */
	private String options = "";

	/**
	 * uglifyjs Command options
	 * 
	 * @parameter property="suffix"
	 */
	private String suffix = "";

	/**
	 * uglifyjs Merge directive
     *
	 * @parameter property="merge"
	 */
	private Boolean merge = Boolean.FALSE;

	/**
	 * Output file for merged result
	 * 
	 * @parameter property="outputFileName"
	 */
	private String outputFileName = "";

	/**
	 * Output file for merged result
	 * 
	 * @parameter property="mainFileName"
	 */
	private String mainFileName = "";

	/**
	 * Other source files to be processed
	 * 
	 * @parameter property="sourceFiles"
	 */
	private String sourceFiles = "";

	/**
	 * Source directory of the JavaScript files
	 * 
	 * @parameter property="jsSourceDir" default-value="${basedir}/src/main/webapp"
	 * @required
	 */
	private File jsSourceDir;

	/**
	 * Output directory for the processed JavaScript files.
	 * 
	 * @parameter property="jsOutputDir" default-value="${project.build.directory}/${project.build.finalName}"
	 * @required
	 */
	private File jsOutputDir;

	/**
	 * Files/folders to exclude from processing
	 * 
	 * @parameter property="jsExcludes" alias="resources"
	 */
	private ArrayList<String> jsExcludes;

	public void execute() throws MojoExecutionException {
		JsFileFilter jsFileFilter = new JsFileFilter(jsExcludes);
		Collection<File> jsFiles = FileUtils.listFiles(jsSourceDir, jsFileFilter, TrueFileFilter.INSTANCE);
		List<File> otherFiles = new LinkedList<File>();
		if( !sourceFiles.isEmpty() ) {
			String[] names = sourceFiles.split( "," );
			for( String name : names ) {
				File f = new File(jsSourceDir,name.trim());
				otherFiles.add( f );
			}
		}
		if( !mainFileName.isEmpty() || !otherFiles.isEmpty() ) {
			jsFiles = reorder( jsFiles, otherFiles );
		}
		if( !merge ) {
			for (File file : jsFiles) {
				try {
					getLog().info("Uglifying from " + file.getPath() );
					Process proc = runUglifyJs2Process(file);
					if(proc.waitFor() != 0) {
						warnOfUglifyJs2Error(proc.getErrorStream(), file.getName());
					}
				 } catch (IOException e) { 
					 throw new MojoExecutionException("Failed to execute uglifyjs process", e);
				 } catch (InterruptedException e) {
					 throw new MojoExecutionException("UglifyJs process interrupted", e);
				 }
			}
		}
		else {
			try {
				Process proc = runUglifyJs2Process(jsFiles);
				if(proc.waitFor() != 0) {
					warnOfUglifyJs2Error(proc.getErrorStream(), jsSourceDir.getAbsolutePath());
				}
			 } catch (IOException e) { 
				 throw new MojoExecutionException("Failed to execute uglifyjs process", e);
			 } catch (InterruptedException e) {
				 throw new MojoExecutionException("UglifyJs process interrupted", e);
			 }			
		}
	}
	
	// Gets the main file then other specified files in given order or gets all files in natural sorted order  
	
	private Collection<File> reorder(Collection<File> inFiles,Collection<File> otherFiles) {
		Collection<File> outFiles = new LinkedList< File >();
		List<File> others = new LinkedList< File >();
		boolean preOrdered = false;
		if( otherFiles != null && !otherFiles.isEmpty() ) {
			others.addAll( otherFiles );
			preOrdered = true;
		}
		for (File file : inFiles) {
			if( mainFileName.equals( file.getName() ) ) {
				outFiles.add( file );
				if( preOrdered ) {
					break;
				}
			}
			else if( !preOrdered ) {
				others.add(file);
			}
		}
		if( !preOrdered ) {
			Collections.sort( others );
		}
		outFiles.addAll( others );
		return outFiles;
	}

	
	private Process runUglifyJs2Process(File inputFile) throws IOException, InterruptedException {
		StringBuilder commandLine = new StringBuilder(command);
		commandLine.append( ' ' ).append( inputFile.getPath() );
		String outFile = this.getOutputFile(inputFile).getPath();
		return runUglifyJs2Command( commandLine, inputFile.getPath(), outFile);
	}
	
	private Process runUglifyJs2Process(Collection<File> jsFiles) throws IOException, InterruptedException {
		StringBuilder commandLine = new StringBuilder(command);
		for (File file : jsFiles) {
			commandLine.append( ' ' ).append( file.getPath() );
			getLog().info("Uglifying from " + file.getPath() );
		}
		String outFileName = outputFileName;
		if( outFileName.isEmpty() ) {
			MavenProject project = (MavenProject)this.getPluginContext().get( "project" );
			outFileName = project.getBuild().getFinalName() + ".js";
		}
		String outFile = this.getOutputFile(jsOutputDir.getPath(),outFileName).getPath();
		return runUglifyJs2Command( commandLine, jsSourceDir.getPath(), outFile);
	}
	
	private Process runUglifyJs2Command(StringBuilder commandLine, String inFile, String outFile) throws IOException, InterruptedException {		
		Runtime rt = Runtime.getRuntime();
		commandLine.append( ' ' ).append( options ).append( " -o " ).append( outFile );
		return rt.exec(commandLine.toString());
	}
	
	
	private void warnOfUglifyJs2Error(InputStream errorStream, String fileName) throws IOException {
		String error = IOUtils.toString(errorStream, "UTF-8");
		getLog().warn("Error while uglifying " + fileName + ":" + error);
		getLog().warn(fileName + " was not uglified. Continuing...");
	}
	
	private final File getOutputFile(File inputFile) throws IOException {
		URI outputURI = jsSourceDir.toURI().relativize(inputFile.getParentFile().toURI());
		StringBuilder sb = new StringBuilder(jsOutputDir.getPath());		
		sb.append(File.separatorChar).append(outputURI.getPath());
		return getOutputFile(sb.toString(),inputFile.getName());
	}

	private final File getOutputFile( String filePath, String fileName ) throws IOException {
		File outputBaseDir = new File( filePath );
		if( !outputBaseDir.exists() ) {
			FileUtils.forceMkdir( outputBaseDir );
		}
		String outFileName = fileName;
		if( outputFileName.isEmpty() && suffix.length() > 0 ) {
			StringBuilder sb = new StringBuilder();
			int lastDot = fileName.lastIndexOf( '.' );
			if( lastDot > 0 ) {
				sb.append( fileName.substring( 0, lastDot ) );
			}
			sb.append( suffix );
			sb.append( fileName.substring( lastDot ) );
			outFileName = sb.toString();
		}
		return new File( outputBaseDir, outFileName );
	}
}