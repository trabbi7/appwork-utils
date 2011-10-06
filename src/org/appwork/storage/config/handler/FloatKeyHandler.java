/**
 * Copyright (c) 2009 - 2011 AppWork UG(haftungsbeschränkt) <e-mail@appwork.org>
 * 
 * This file is part of org.appwork.storage.config
 * 
 * This software is licensed under the Artistic License 2.0,
 * see the LICENSE file or http://www.opensource.org/licenses/artistic-license-2.0.php
 * for details
 */
package org.appwork.storage.config.handler;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

import org.appwork.storage.StorageException;
import org.appwork.storage.config.annotations.DefaultBooleanValue;
import org.appwork.storage.config.annotations.DefaultByteValue;
import org.appwork.storage.config.annotations.DefaultFloatValue;
import org.appwork.storage.config.annotations.DefaultLongValue;
import org.appwork.storage.config.annotations.SpinnerValidator;

/**
 * @author Thomas
 *
 */
public class FloatKeyHandler extends KeyHandler<Float> {

    /**
     * @param storageHandler
     * @param key
     */
    public FloatKeyHandler(StorageHandler<?> storageHandler, String key) {
        super(storageHandler, key);
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#putValue(java.lang.Object)
     */
    @Override
    protected void putValue(Float object) {

                this.storageHandler.putPrimitive(this.setter.getKey(), (Float) object);
         
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#initHandler()
     */
    @Override
    protected void initHandler() {
        try{
            this.defaultValue = getAnnotation(DefaultFloatValue.class).value();
           }catch(NullPointerException e){
               defaultValue=0f;
               
           }
    }
    
    @Override
    protected boolean initDefaults() throws Throwable {
        defaultValue=0f;
       return super.initDefaults();
    }
    
    @Override
    protected Class<? extends Annotation> getDefaultAnnotation() {

        return DefaultFloatValue.class;
    }
    
    /* (non-Javadoc)
     * @see org.appwork.storage.config.KeyHandler#validateValue(java.lang.Object)
     */
    @Override
    protected void validateValue(Float object) throws Throwable {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.appwork.storage.config.handler.KeyHandler#getValue()
     */
    @Override
    public Float getValue() {
        return primitiveStorage.get(getKey(), defaultValue);
    }

}