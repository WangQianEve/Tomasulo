package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Memory {
    private int size;
    private float value[];

    Memory(){
        this(1024);
    }

    Memory(int size){
        this.size = size;
        this.value = new float[size];
        this.reset();
    }

    public void reset(){
        for (int i = 0; i < size; i++)
            value[i] = 0;
        this.setValue(1024, 4);
        this.setValue(1028, 8);
    }

    public void setValue(int index, float v){
        if(index/4 < size){
            value[index/4] = v;
        }
        else
            System.out.println("Error : index out of memory range");
    }

    public float getValue(int index) {
        if (index / 4 < size) {
            return value[index / 4];
        } else {
            System.out.println("Error : index out of memory range");
            return 0;
        }
    }

}
