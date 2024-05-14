package traffic4.objects;

import rescuecore2.misc.geometry.Line2D;

public class IgnoreBlockadesAgent {
    /**
     * This class is used to compute ground related information
     */

    private static class GroundInfo {
        private Line2D ground;
        private TrafficArea1 area;
        private double height;


        public GroundInfo(Line2D ground, TrafficArea1 area) {
            this.ground = ground;
            this.height = 200;
            this.area = area;
        }
    }
}
