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

package org.nanoko.coffee.mill.mojos.compile;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.nanoko.coffee.mill.mojos.AbstractCoffeeMillMojo;
import org.nanoko.coffee.mill.processors.JSHintProcessor;
import org.nanoko.coffee.mill.processors.JSLintProcessor;
import org.nanoko.coffee.mill.processors.JavaScriptFileCopyProcessor;
import org.nanoko.coffee.mill.processors.Processor;
import org.nanoko.coffee.mill.utils.OptionsHelper;

import java.util.Map;

/**
 * Copy JavaScript sources to the <tt>work</tt> directory and check JavaScript sources with
 * <ul>
 *     <li>Check the code using JSLint</li>
 *     <li>Check the code using JSHint</li>
 * </ul>
 * TODO Exclude strict mode.
 *
 * @goal test-compile-javascript
 *
 */
public class JavaScriptTestCompilerMojo extends AbstractCoffeeMillMojo {

    /**
     * Sets to true to disable JSLint
     *
     * @parameter default-value="true"
     */
    protected boolean skipJsLint;

    /**
     * Sets to true to disable JSHint
     *
     * @parameter default-value="true"
     */
    protected boolean skipJsHint;

    /**
     * JSHint configuration options.
     * @parameter
     */
    private JSHintOptions jshintOptions;


    public void execute() throws MojoExecutionException, MojoFailureException {
        if (! javaScriptTestDir.exists()) {
            getLog().info("The javascript test directory does not exist - skipping JavaScript compilation");
            return;
        }

        JavaScriptFileCopyProcessor processor = new JavaScriptFileCopyProcessor();
        processor.configure(this, new OptionsHelper.OptionsBuilder().set("test", true).build());
        try {
            processor.processAll();
        } catch (Processor.ProcessorException e) {
            throw new MojoExecutionException("Cannot copy JavaScript files", e);
        }

        if (! skipJsLint) {
            doJsLint();
        } else {
            getLog().debug("JS Lint skipped");
        }

        if (! skipJsHint) {
            doJsHint();
        } else {
            getLog().debug("JS Hint skipped");
        }

    }

    private void doJsLint() throws MojoExecutionException {
        getLog().info("Checking sources with JsLint");
        JSLintProcessor processor = new JSLintProcessor();
        Map<String,Object> options = null;
        if(jshintOptions != null){
            options = new OptionsHelper.OptionsBuilder().set(JSHintProcessor.JSHINT_OPTIONS_KEY, jshintOptions.format()).build();
        }
        processor.configure(this, options);
        try {
            processor.processAll();
        } catch (Processor.ProcessorException e) {
            throw new MojoExecutionException("", e);
        }
    }

    private void doJsHint() throws MojoExecutionException {
        getLog().info("Checking sources with JsHint");
        JSHintProcessor processor = new JSHintProcessor();
        processor.configure(this, null);
        try {
            processor.processAll();
        } catch (Processor.ProcessorException e) {
            throw new MojoExecutionException("", e);
        }
    }
}
