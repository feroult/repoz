package com.murerz.repoz.web.fs;

import java.util.Set;

public interface FileSystem {

	RepozFile read(String path);

	void save(RepozFile file);

	void delete(String path);

	void deleteAll();

	Set<String> listRepositories();

}
