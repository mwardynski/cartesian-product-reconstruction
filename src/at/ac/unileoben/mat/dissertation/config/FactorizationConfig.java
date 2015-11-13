package at.ac.unileoben.mat.dissertation.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 06.11.15
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
@Configuration
@ComponentScan("at.ac.unileoben.mat.dissertation")
@EnableAspectJAutoProxy
public class FactorizationConfig
{
}
