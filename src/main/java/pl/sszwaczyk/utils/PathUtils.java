package pl.sszwaczyk.utils;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.routing.Path;
import org.projectfloodlight.openflow.types.DatapathId;

import java.util.ArrayList;
import java.util.List;

public class PathUtils {

    public static List<DatapathId> getSwitchesFromPath(Path path) {
        List<NodePortTuple> npts = path.getPath();
        List<DatapathId> switches = new ArrayList<>();
        for(int i = 0; i < npts.size() - 1; i = i + 2) {
            DatapathId s1 = npts.get(i).getNodeId();
            DatapathId s2 = npts.get(i + 1).getNodeId();

            if(i == 0) {
                switches.add(s1);
            }

            switches.add(s2);

        }

        return switches;
    }
}
