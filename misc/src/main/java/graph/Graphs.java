package graph;

import de.uniulm.omi.cloudiator.domain.ApplicationInstance;

/**
 * Created by daniel on 17.02.17.
 */
public class Graphs {

    private Graphs() {
        throw new AssertionError("Do not instantiate");
    }

    public static ApplicationInstanceGraph applicationInstanceGraph(ApplicationInstance applicationInstance) {
            return new ApplicationInstanceGraph(applicationInstance);
    }

}
