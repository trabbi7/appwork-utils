/**
 * Copyright (c) 2009 - 2012 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.txtresource
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.txtresource;

import java.lang.reflect.Method;

/**
 * @author Thomas
 * 
 */
public class TranslationSource {
    private TranslateResource resource;

    public TranslateResource getResource() {
        return resource;
    }

    public boolean isFromLngFile() {
        return fromLngFile;
    }
public String toString(){
    if(!isFromLngFile()){
        return "["+resource.getName()+"] "+"Default Annotations: "+method.getName();
    }else{
        return "["+resource.getName()+"] "+resource.getUrl();   
    }
}
    private boolean fromLngFile;
    private Method method;

    /**
     * @param translateResource
     * @param method 
     * @param b
     */
    public TranslationSource(TranslateResource translateResource, Method method, boolean fromLngFile) {
        resource = translateResource;
        this.method=method;
        this.fromLngFile = fromLngFile;
    }

    /**
     * @return
     */
    public String getID() {
        // TODO Auto-generated method stub
        return resource.getName();
    }

}