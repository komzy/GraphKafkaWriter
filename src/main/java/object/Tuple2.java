package object;

public class Tuple2<T0, T1> {
    private static final long serialVersionUID = 1L;
    public T0 f0;
    public T1 f1;

    public Tuple2() {
    }

    public Tuple2(T0 f0, T1 f1) {
        this.f0 = f0;
        this.f1 = f1;
    }

    public int getArity() {
        return 2;
    }

    public void setFields(T0 f0, T1 f1) {
        this.f0 = f0;
        this.f1 = f1;
    }

    public Tuple2<T1, T0> swap() {
        return new Tuple2(this.f1, this.f0);
    }

    public String toString() {
        return "(" + this.f0 + "," + this.f1 + ")";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Tuple2)) {
            return false;
        } else {
            Tuple2 tuple = (Tuple2)o;
            if (this.f0 != null) {
                if (!this.f0.equals(tuple.f0)) {
                    return false;
                }
            } else if (tuple.f0 != null) {
                return false;
            }

            if (this.f1 != null) {
                if (!this.f1.equals(tuple.f1)) {
                    return false;
                }
            } else if (tuple.f1 != null) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int result = this.f0 != null ? this.f0.hashCode() : 0;
        result = 31 * result + (this.f1 != null ? this.f1.hashCode() : 0);
        return result;
    }

    public Tuple2<T0, T1> copy() {
        return new Tuple2(this.f0, this.f1);
    }

    public static <T0, T1> Tuple2<T0, T1> of(T0 f0, T1 f1) {
        return new Tuple2(f0, f1);
    }
}