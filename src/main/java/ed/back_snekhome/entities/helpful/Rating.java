package ed.back_snekhome.entities.helpful;

import ed.back_snekhome.enums.RatingType;

public abstract class Rating {

    private RatingType type;

    public abstract RatingType getType();
    public abstract void setType(RatingType type);
}
