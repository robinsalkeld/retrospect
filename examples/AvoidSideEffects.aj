package examples;

public aspect AvoidSideEffects {

    pointcut myAspectCflow() : cflow(adviceexecution() && within(MyAspect));
    
    around() : myAspectCflow() && get(* System.out) {
        
    }
}
