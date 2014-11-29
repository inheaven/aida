package ru.inhell.aida.common.jquery;

import org.apache.wicket.request.resource.CssResourceReference;

/**
 * inheaven on 30.11.2014 4:07.
 */
public class JQueryBootstrapThemeResourceReference extends CssResourceReference {
    public JQueryBootstrapThemeResourceReference() {
        super(JQueryBootstrapThemeResourceReference.class, "custom-theme/jquery-ui-1.10.0.custom.css");
    }
}
