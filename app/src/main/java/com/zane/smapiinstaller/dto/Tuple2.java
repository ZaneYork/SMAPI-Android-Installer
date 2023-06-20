package com.zane.smapiinstaller.dto;

public class Tuple2<U, V> {
    private U first;
    private V second;

    //<editor-fold defaultstate="collapsed" desc="delombok">
    @SuppressWarnings("all")
    public U getFirst() {
        return this.first;
    }

    @SuppressWarnings("all")
    public V getSecond() {
        return this.second;
    }

    @SuppressWarnings("all")
    public void setFirst(final U first) {
        this.first = first;
    }

    @SuppressWarnings("all")
    public void setSecond(final V second) {
        this.second = second;
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Tuple2)) return false;
        final Tuple2<?, ?> other = (Tuple2<?, ?>) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$first = this.getFirst();
        final Object other$first = other.getFirst();
        if (this$first == null ? other$first != null : !this$first.equals(other$first)) return false;
        final Object this$second = this.getSecond();
        final Object other$second = other.getSecond();
        if (this$second == null ? other$second != null : !this$second.equals(other$second)) return false;
        return true;
    }

    @SuppressWarnings("all")
    protected boolean canEqual(final Object other) {
        return other instanceof Tuple2;
    }

    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $first = this.getFirst();
        result = result * PRIME + ($first == null ? 43 : $first.hashCode());
        final Object $second = this.getSecond();
        result = result * PRIME + ($second == null ? 43 : $second.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("all")
    public String toString() {
        return "Tuple2(first=" + this.getFirst() + ", second=" + this.getSecond() + ")";
    }

    @SuppressWarnings("all")
    public Tuple2(final U first, final V second) {
        this.first = first;
        this.second = second;
    }
    //</editor-fold>
}
