package io.github.sskorol.config;

import io.github.sskorol.core.Browser;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.edge.EdgeOptions;

public class Edge implements Browser {

    public Name name() {
        return Name.Edge;
    }


    @Override
    public Capabilities configuration(final XmlConfig context) {
        return merge(context, new EdgeOptions());
    }
}
