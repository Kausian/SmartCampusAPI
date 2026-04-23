package com.westminster.smartcampus.config;

import com.westminster.smartcampus.filter.ApiLoggingFilter;
import com.westminster.smartcampus.mapper.GlobalExceptionMapper;
import com.westminster.smartcampus.mapper.LinkedResourceNotFoundMapper;
import com.westminster.smartcampus.mapper.RoomNotEmptyMapper;
import com.westminster.smartcampus.mapper.SensorUnavailableMapper;
import com.westminster.smartcampus.resource.DebugResource;
import com.westminster.smartcampus.resource.DiscoveryResource;
import com.westminster.smartcampus.resource.RoomResource;
import com.westminster.smartcampus.resource.SensorResource;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(DebugResource.class);

        classes.add(RoomNotEmptyMapper.class);
        classes.add(LinkedResourceNotFoundMapper.class);
        classes.add(SensorUnavailableMapper.class);
        classes.add(GlobalExceptionMapper.class);

        classes.add(ApiLoggingFilter.class);

        return classes;
    }
}