package convertor;

public class AbstractClass {
    int z;
    public void calc(int x, int y){
        z = x * y;
    }
    public void print(){
        System.out.print("The result is: " + z);
    }
}
