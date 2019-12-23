package com.wangym.lombok.job;

import lombok.Getter;

@Getter
public class WordEntity {

    private String name;
    private int count;
    
    public void incr() {
        count++;
    }

    public WordEntity(String name) {
        super();
        this.name = name;
        this.count = 1;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if(obj instanceof String) {
            return name.equals(obj);
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WordEntity other = (WordEntity) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name + ", count=" + count + "]";
    }
    
    
}
