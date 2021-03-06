/*
 * Copyright 2013 OW2 Nanoko Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.nanoko.coffee.mill.mojos.reporting;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.nanoko.coffee.mill.utils.ExecUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Builds the JSDoc API.
 * It uses JSDoc3 but <strong>requires</strong> to have the <tt>jsdoc</tt> executable in the path.
 *
 * @goal jsdoc
 * @phase site
 */
public class JsDocMojo extends AbstractMavenReport {

    /**
     * @parameter default-value="false"
     */
    protected boolean skipJSDOC;

    /**
     * Whether to include symbols tagged as private. Default is <code>false</code>.
     *
     * @parameter expression="false"
     */
    protected boolean jsdocIncludePrivate;

    /**
     * Directory where reports will go.
     *
     * @parameter expression="${project.reporting.outputDirectory}"
     * @required
     * @readonly
     */
    protected String outputDirectory;

    /**
     * @parameter default-value="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    protected Renderer siteRenderer;


    public void execute() throws MojoExecutionException {
        try {
            executeReport(null);
        } catch (MavenReportException e) {
            throw new MojoExecutionException("Cannot build JSDOC report", e);
        }
    }

    @Override
    protected Renderer getSiteRenderer() {
        return siteRenderer;
    }

    public String getName(Locale locale) {
        return "jsdoc";
    }

    public String getDescription(Locale locale) {
        return "Generate JSDOC report";
    }

    @Override
    protected String getOutputDirectory() {
        return this.outputDirectory + "/jsdoc";
    }

    @Override
    protected MavenProject getProject() {
        return project;
    }

    @Override
    protected void executeReport(Locale locale) throws MavenReportException {
        generateJSDOC();
    }

    private void generateJSDOC() throws MavenReportException {
        if (skipJSDOC) {
            getLog().info("JSDoc report generation skipped");
            return;
        }


        File jsdocExec = ExecUtils.findExecutableInPath("jsdoc");
        if (jsdocExec == null) {
            getLog().error("Cannot build jsdoc report - jsdoc not in the system path, the report is ignored.");
            return;
        } else {
            getLog().info("Invoking jsdoc : " + jsdocExec.getAbsolutePath());
            getLog().info("Output directory : " + getOutputDirectory());
        }

        File out = new File(getOutputDirectory());
        out.mkdirs();

        CommandLine cmdLine = CommandLine.parse(jsdocExec.getAbsolutePath());

        // Destination
        cmdLine.addArgument("--destination");
        String destPath = out.getAbsolutePath();
        // Escapes spaces with a \
        //destPath = destPath.replace(" ", "\\ ");
        cmdLine.addArgument(destPath, false);

        if (jsdocIncludePrivate) {
            cmdLine.addArgument("--private");
        }

        File input = new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".js");
        if (! input.exists()) {
            throw new MavenReportException("Cannot find the project's artifact : " + input.getAbsolutePath());
        }
        String inputPath = input.getAbsolutePath();
        // Escapes spaces with a \
        //inputPath = inputPath.replace(" ", "\\ ");
        cmdLine.addArgument(inputPath, false);

        DefaultExecutor executor = new DefaultExecutor();

        executor.setWorkingDirectory(project.getBasedir());
        executor.setExitValue(0);
        try {
            for (String s : cmdLine.toStrings()) {
                System.out.println(s);
            }
            getLog().info("Executing " + cmdLine.toString());
            executor.execute(cmdLine);
        } catch (IOException e) {
            throw new MavenReportException("Error during jsdoc report generation", e);
        }


    }

    public String getOutputName() {
        return "jsdoc" + "/index";
    }

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#isExternalReport()
     */
    public boolean isExternalReport() {
        return true;
    }

}
