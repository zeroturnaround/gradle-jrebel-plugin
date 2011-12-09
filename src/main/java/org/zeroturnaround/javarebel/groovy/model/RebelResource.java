package org.zeroturnaround.javarebel.groovy.model;

import java.util.List;

public interface RebelResource {

	List<String> getIncludes();

	void setIncludes(List<String> includes);

	List<String> getExcludes();

	void setExcludes(List<String> excludes);

}
