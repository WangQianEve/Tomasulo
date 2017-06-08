package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Memory_unit {


    public Memory_unit() {
        reset();
    }

    public int getRs_id() {
        return rs_id;
    }

    public void setRs_id(int rs_id) {
        this.rs_id = rs_id;
    }

    public int getEnd_time() {
        return end_time;
    }

    public void setEnd_time(int end_time) {
        this.end_time = end_time;
    }

    public float getResult() {
        return result;
    }

    public void setResult(float result) {
        this.result = result;
    }

    public boolean isBusy() {
        return busy;
    }

    public void setBusy(boolean busy) {
        this.busy = busy;
    }

    private int rs_id;
    private int end_time;
    private float result;
    private boolean busy;

    public void reset() {
        rs_id = -1;
        end_time = 0;
        result = 0;
        busy = false;
    }
}
