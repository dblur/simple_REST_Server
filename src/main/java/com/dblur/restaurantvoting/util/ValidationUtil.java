package com.dblur.restaurantvoting.util;

import com.dblur.restaurantvoting.exception.IllegalRequestDataException;
import com.dblur.restaurantvoting.model.BaseEntity;

public class ValidationUtil {

    public static void checkNew(BaseEntity entity) {
        if (!entity.isNew()) {
            throw new IllegalRequestDataException(entity.getClass().getSimpleName() + " must be new (id=null)");
        }
    }

    public static void assureIdConsistent(BaseEntity entity, Long id) {
        if (entity.isNew()) {
            entity.setId(id);
        } else if (!entity.id().equals(id)) {
            throw new IllegalRequestDataException(entity.getClass().getSimpleName() + " must has id = " + id);
        }
    }
}
