package com.lfj.dome.framework.webmvc;

import java.io.File;
import java.net.URL;

/**
 *  自定义ViewResolver
 */
public class LViewResolver {


    private final String DEFAULT_TEMPLATE_SUFFIX = ".html";

    private File tempateRootDir;


    public LViewResolver(String templateRoot) {
        URL resource = this.getClass().getClassLoader().getResource(templateRoot);
        String templateRootPath = resource.getFile();

        tempateRootDir = new File(templateRootPath);
    }

    public LView resolveViewName(String viewName){
        if(null == viewName || "".equals(viewName.trim())){return null;}
        viewName = viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)? viewName : (viewName + DEFAULT_TEMPLATE_SUFFIX);
        File templateFile = new File((tempateRootDir.getPath() + "/" + viewName).replaceAll("/+","/"));
        return new LView(templateFile);
    }


}
