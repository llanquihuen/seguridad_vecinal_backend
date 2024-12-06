package cl.seguridad.vecinal.dao;

import cl.seguridad.vecinal.modelo.Usuario;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM usuario WHERE email = #{email}")
    public Usuario findByEmail(@Param("email") String email);
}