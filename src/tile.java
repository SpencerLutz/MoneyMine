import java.util.*;

class tile {

    public boolean wall; public int gem, mine, x, y, cost, yield;

    public tile(boolean wall, int gem, int x, int y) {
        this.wall = wall; this.gem = gem;
        this.x = x; this.y = y;
    }

    public ArrayList<Tiles> image() {
        ArrayList<Tiles> img = new ArrayList<>();
        img.add(Tiles.values()[gem+9]);
        if(mine > 0) img.add(Tiles.values()[mine]);
        if(wall) img.add(Tiles.WA);
        return img;
    }

    public void updateCost(int cx, int cy) {
        if(wall) cost = (int) (5.0 * Math.pow(1.12982271, (Math.abs(x - cx) + Math.abs(y - cy))));
        else cost = gem * (int) (10.0 * Math.pow(100, mine));
    }

    public void updateYield() {
        yield = getYield(mine);
    }

    public int getYield(int level) {
        return (int) Math.pow(4, level-1);
    }

    public String[] description() {
        ArrayList<String> reta = new ArrayList<>();
        if(wall) reta.add("Cave Wall");
        else {
            String[] c = {"Red ", "Orange ", "Yellow ", "Green ", "Blue ", "Purple "};
            reta.add(c[gem-1] + "Gems");
        }
        if(mine > 0) reta.add("Mine Level " + Integer.toString(mine)
                + " (" + Integer.toString(yield) + "/s)");
        if(wall) reta.add("Destroy Wall: $" + Integer.toString(cost));
        else if(mine < 1) reta.add("Build Mine:");
        else if(mine < 3) reta.add("Upgrade Mine:");
        if(!wall && mine < 3) reta.add("$" + Integer.toString(cost)
                + " (" + Integer.toString(getYield(mine+1)) + "/s)");
        String[] retb = new String[reta.size()];
        reta.toArray(retb); return retb;
    }
}