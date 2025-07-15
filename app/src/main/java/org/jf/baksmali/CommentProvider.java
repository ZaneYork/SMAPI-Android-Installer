package org.jf.baksmali;

import java.util.Map;

public abstract class CommentProvider {
    public CommentProvider(){

    }
    public abstract String getComment(int resourceId);

    public static class CommentMap extends CommentProvider{
        private final Map<Integer, String> map;
        public CommentMap(Map<Integer, String> map){
            super();
            this.map = map;
        }
        @Override
        public String getComment(int resourceId){
            return map.get(resourceId);
        }
    }
}
