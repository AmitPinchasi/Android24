class F{
    int a;
}

class S extends F{
    int a;
    void set(){
        a = 1;
        this.a = 2;
        super.a = 3;
    }

    void print(){
        System.out.println(a + this.a + super.a);
    }
}



public class Test {
    public static void main(String[] args){
        S s = new S();
        s.a = 5;
        System.out.println(s.a);
    }
}
