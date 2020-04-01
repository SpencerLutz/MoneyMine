import java.util.*;
//Fluctuating market prices (Sell button)
//More valuable gems spawn less frequently and less numerously and longer wait times
//Elevator + miners
//Wall costs based on walking distance
//Limited amount of gems mineable per tile 
//Improved graphics (more tiles, sig fig #s)
//Sound
//More buildings (Piping, Storages, etc)
//Some sort of competition/enemy
public class game {
    public int mW = 101, mH = 101, money = 10;
    public int[] gems = new int[6], gpt = new int[6], price = new int[6], market = new int[6];
    public tile[][] map = new tile[mW][mH];
    public boolean[] unlocked = new boolean[6];
    public ArrayList<tile> all = new ArrayList<>();
    public int sx = mW/2, sy = mH/2;

    public void gen(double mfreq, int gsize) {
        for(int i = 0; i < 6; i++){
            market[i] = 3997/(int)Math.pow(i+1, 3); unlocked[i] = false;
        }
        updatePrices();
        for(int x = 0; x < mW; x++) for(int y = 0; y < mH; y++){
            map[x][y] = new tile(true, 0, x, y);
            map[x][y].updateCost(sx, sy);
            if(Math.random()<=(mfreq/(mW*mH)))
                map[x][y].gem = gemlevel(x, y);
        }
        map[sx][sy] = new tile(false, 1, sx, sy);
        for(int n = 0; n < gsize * 3.5; n++)
            for(int x = 0; x < mW; x++) for(int y = 0; y < mH; y++)
                if(Math.random()<.02 && map[x][y].gem == 0) map[x][y].gem = nbgem(x, y);
        map[sx][sy].updateCost(sx, sy);
        smash(map[sx][sy]);
    }

    public int nbgem(int x, int y) {
        if(x > 0 && map[x-1][y].gem != 0) return map[x-1][y].gem;
        if(x < mW-1 && map[x+1][y].gem != 0) return map[x+1][y].gem;
        if(y > 0 && map[x][y-1].gem != 0) return map[x][y-1].gem;
        if(y < mH-1 && map[x][y+1].gem != 0) return map[x][y+1].gem;
        return 0;
    }

    public int gemlevel(int x, int y) {
        int maxgem = (int) Math.ceil(6.0 * (Math.abs(x - sx) + Math.abs(y - sy)) / sx);
        if(maxgem > 6) maxgem = 6;
        int[] agems = {maxgem, maxgem-1, maxgem-2};
        for(int i = 1; i < 3; i++) if(agems[i] < 1) agems[i] = agems[0];
        return agems[(int)(Math.random()*3)];
    }

    public void smash(tile t) {
        int x = t.x, y = t.y;
        map[x][y].wall = false;
        if(!all.contains(t)) all.add(t);
        if(!all.contains(map[x-1][y])) all.add(map[x-1][y]);
        if(!all.contains(map[x+1][y])) all.add(map[x+1][y]);
        if(!all.contains(map[x][y-1])) all.add(map[x][y-1]);
        if(!all.contains(map[x][y+1])) all.add(map[x][y+1]);
    }

    public void updatePrices(){
        for(int i = 0; i < 6; i++){
            if(market[i] > 0) price[i] = (int) Math.pow(i+2, 3)/market[i];
            if(price[i] > 999) price[i] = 999;
            if(price[i] < 1) price[i] = 1;
        }
    }

    public boolean show(tile t) {
        return all.contains(t);
    }

    public void update(boolean bucks) {
        for(int i = 0; i < 6; i++){
            if(Math.random()<.5){
                if(Math.random()<price[i]/1000.0) market[i] += Math.random()*(30/(i+1));
                else market[i] -= Math.random()*(29/(i+1));
            }
            market[i] = market[i]<1?0:market[i]; updatePrices();
            if(bucks) gems[i] += gpt[i];
        }
    }
}