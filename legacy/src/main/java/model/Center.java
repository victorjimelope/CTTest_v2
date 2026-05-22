package model;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @EqualsAndHashCode(callSuper = true)
public class Center extends BaseEntity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	
}
