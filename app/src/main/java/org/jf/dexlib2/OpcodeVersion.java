package org.jf.dexlib2;

import java.util.Collection;

public class OpcodeVersion {
    public static class VersionConstraint {
        public final VersionRange apiRange;
        public final VersionRange artVersionRange;
        public final int opcodeValue;

        public VersionConstraint(VersionRange apiRange, VersionRange artVersionRange,
                                 int opcodeValue) {
            this.apiRange = apiRange;
            this.artVersionRange = artVersionRange;
            this.opcodeValue = opcodeValue;
        }
    }

    public static VersionRange atMost(int version){
        return new OpcodeVersion.VersionRange(null, version);
    }
    public static VersionRange openClosed(int min, int max){
        return new OpcodeVersion.VersionRange(min + 1, max);
    }
    public static VersionRange closedOpen(int min, int max){
        return new OpcodeVersion.VersionRange(min, max - 1);
    }
    public static VersionRange closed(int min, int max){
        return new OpcodeVersion.VersionRange(min, max);
    }
    public static VersionRange open(int min, int max){
        return new OpcodeVersion.VersionRange(min + 1, max - 1);
    }
    public static VersionRange atLeast(int version){
        return new OpcodeVersion.VersionRange(version, null);
    }
    public static final VersionRange ALL = new VersionRange(null, null) {
        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(int version) {
            return true;
        }

        @Override
        public void setMin(Integer min) {
        }

        @Override
        public void setMax(Integer max) {
        }
    };
    public static final VersionRange NONE = new VersionRange(1, 0) {
        @Override
        public boolean isEmpty() {
            return true;
        }
        @Override
        public boolean contains(int version) {
            return false;
        }
        @Override
        public void setMin(Integer min) {
        }
        @Override
        public void setMax(Integer max) {
        }
    };
    

    public static class VersionRange {
        private Integer min;
        private Integer max;

        public VersionRange(Integer min, Integer max){
            this.min = min;
            this.max = max;
        }
        public boolean isEmpty() {
            if(this.min == null && this.max == null){
                return false;
            }
            if(this.min != null && this.max != null){
                return this.min > this.max;
            }
            if(this.min == null && this.max <= 0){
                return true;
            }
            return false;
        }
        public boolean contains(int version){
            if(min != null && version < min){
                return false;
            }
            if(max != null && version > max){
                return false;
            }
            return true;
        }
        public boolean isAll(){
            return this.min == null && this.max == null;
        }
        public Integer getMin() {
            return min;
        }
        public void setMin(Integer min) {
            this.min = min;
        }
        public Integer getMax() {
            return max;
        }
        public void setMax(Integer max) {
            this.max = max;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            if(this.min == null){
                builder.append("-\u221e");
            }else {
                builder.append(this.min);
            }
            builder.append(" .. ");
            if(this.max == null){
                builder.append("+\u221e");
            }else {
                builder.append(this.max);
            }
            builder.append(')');
            return builder.toString();
        }
    }

    public static class VersionRangeMap {
        private final VersionValue[] versionValues;
        public VersionRangeMap(VersionValue[] versionValues){
            this.versionValues = versionValues;
        }
        public VersionRangeMap(Collection<VersionValue> collection){
            this.versionValues = collection.toArray(new VersionValue[0]);
        }
        public Short get(int version){
            VersionValue versionValue = getVersionValue(version);
            if(versionValue != null){
                return versionValue.value;
            }
            return null;
        }
        public VersionValue getVersionValue(int version){
            for(VersionValue value : versionValues){
                if(value.contains(version)){
                    return value;
                }
            }
            return null;
        }
    }

    public static class VersionValue {
        public final VersionRange versionRange;
        public short value;
        public VersionValue(VersionRange range, short value){
            this.versionRange = range;
            this.value = value;
        }
        public boolean contains(int version){
            return versionRange.contains(version);
        }
        @Override
        public String toString() {
            return versionRange + " = " + value;
        }
    }
}
