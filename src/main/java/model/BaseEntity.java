package model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class BaseEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
	protected Integer id;
    
	protected LocalDateTime addDate;
    
	protected Integer addUser;
    
	protected LocalDateTime modDate;
    
	protected Integer modUser;
    
}
