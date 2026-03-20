package com.dreggcake.src.Database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultHandler<T>{
    T handle(ResultSet rs) throws SQLException;
}
