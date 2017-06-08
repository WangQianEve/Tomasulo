package simulator;

/**
 * Created by bob35 on 2017/6/8.
 */
public class Registers {
    private int size;
    private float value[];
    private int Qi[];

    public Registers() {
        this(32);
    }

    public Registers(int size) {
        this.size = size;
        value = new float[size];
        Qi = new int[size];
        this.reset();
    }

    public void setQi(int index, int v){
        if (index < size)
            this.Qi[index] = v;
        else
            System.out.println("Error : index out of registers range");
    }

    public int getQi(int index){
        if (index < size)
            return Qi[index];
        else
            return 0;
    }

    public void setValue(int index, float v){
        if (index < size)
            this.value[index] = v;
        else
            System.out.println("Error : index out of registers range");
    }

    public float getValue(int index){
        if (index < size)
            return this.value[index];
        else
            return 0;
    }

    public void reset(){
        for (int i = 0; i < size; i++){
            Qi[i] = 0;
            value[i] = 0;
        }
    }
}
