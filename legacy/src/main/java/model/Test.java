package model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true)
public class Test extends TestBase implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Question> questions;
	
}
