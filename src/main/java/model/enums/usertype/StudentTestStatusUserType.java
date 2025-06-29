package model.enums.usertype;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import model.enums.StudentTestStatus;

public class StudentTestStatusUserType implements UserType {

    private static final int SQL_TYPE = Types.INTEGER;

    @Override
    public int[] sqlTypes() {
        return new int[]{SQL_TYPE};
    }
    
    @Override
    public Class<StudentTestStatus> returnedClass() {
        return StudentTestStatus.class;
    }

    @Override
    public boolean equals(Object x, Object y) {
        return x == y || (x != null && x.equals(y));
    }

    @Override
    public int hashCode(Object x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Object deepCopy(Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object nullSafeGet(ResultSet rs, String[] names,
    		SharedSessionContractImplementor session, Object owner)
    				throws HibernateException, SQLException {
        int id = rs.getInt(names[0]);
        return rs.wasNull() ? null : StudentTestStatus.getById(id);
    }
    
    @Override
    public void nullSafeSet(PreparedStatement st, Object value,
    		int index, SharedSessionContractImplementor session)
    				throws HibernateException, SQLException {
        st.setObject(index, value != null ? ((StudentTestStatus) value).getId() : null, SQL_TYPE);
    }
    
    @Override
    public Serializable disassemble(Object value) {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) {
        return original;
    }
    
}
