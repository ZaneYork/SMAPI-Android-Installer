/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.graph;

import com.reandroid.common.DiagnosticMessage;
import com.reandroid.common.DiagnosticsReporter;

public abstract class GraphTask implements DiagnosticsReporter {

    private DiagnosticsReporter reporter;
    private String reporterTag;

    public GraphTask() {
        this.reporterTag = getClass().getSimpleName();
    }

    public abstract void apply();

    public DiagnosticsReporter getReporter() {
        return reporter;
    }
    public GraphTask setReporter(DiagnosticsReporter reporter) {
        if(reporter == this) {
            throw new IllegalArgumentException("Can not set reporter to itself");
        }
        this.reporter = reporter;
        return this;
    }

    public String getReporterTag() {
        return reporterTag;
    }
    public GraphTask setReporterTag(String reporterTag) {
        this.reporterTag = reporterTag;
        return this;
    }

    public void info(String message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null && reporter.isReportEnabled()) {
            reporter.report(new DiagnosticMessage.StringMessage(
                    DiagnosticMessage.Type.INFO,
                    getReporterTag(),
                    message));
        }
    }
    public void warn(String message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null && reporter.isReportEnabled()) {
            reporter.report(new DiagnosticMessage.StringMessage(
                    DiagnosticMessage.Type.WARN,
                    getReporterTag(),
                    message));
        }
    }
    public void verbose(String message) {
        if(isVerboseEnabled()) {
            DiagnosticsReporter reporter = getReporter();
            if(reporter != null) {
                reporter.report(new DiagnosticMessage.StringMessage(
                        DiagnosticMessage.Type.VERBOSE,
                        getReporterTag(),
                        message));
            }
        }
    }
    public void debug(String message) {
        if(isDebugEnabled()) {
            DiagnosticsReporter reporter = getReporter();
            if(reporter != null) {
                reporter.report(new DiagnosticMessage.StringMessage(
                        DiagnosticMessage.Type.DEBUG,
                        getReporterTag(),
                        message));
            }
        }
    }
    @Override
    public void report(DiagnosticMessage message) {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null && reporter.isReportEnabled()) {
            reporter.report(message);
        }
    }

    @Override
    public boolean isReportEnabled() {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            return reporter.isReportEnabled();
        }
        return false;
    }
    @Override
    public boolean isVerboseEnabled() {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            return isReportEnabled() && reporter.isVerboseEnabled();
        }
        return false;
    }
    @Override
    public boolean isDebugEnabled() {
        DiagnosticsReporter reporter = getReporter();
        if(reporter != null) {
            return isReportEnabled() && reporter.isDebugEnabled();
        }
        return false;
    }
}
