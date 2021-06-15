package mapperplugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

/**
 *
 * @author Pieter van den Hombergh {@code pieter.van.den.hombergh@gmail.com}
 */
public class FileNameTest {

//@Disabled("think TDD")
    @Test
    public void tGetSourceFiles() {
        SebiMapperGeneratorMojo mojo = new SebiMapperGeneratorMojo();
        String[] sourceFiles = mojo.getSourceFiles( "src/test" );
        assertThat( sourceFiles ).contains(
                "src/test/java/entities/Course.java",
                "src/test/java/entities/Door.java",
                "src/test/java/entities/Engine.java",
                "src/test/java/entities/Person.java",
                "src/test/java/entities/Student.java",
                "src/test/java/entities/Tutor.java"
        );
//        fail( "method tGetSourceFiles reached end. You know what to do." );
    }

    //@Disabled("think TDD")
    @Test
    public void tCompilerArgs() throws DependencyResolutionRequiredException {
        MavenProject p = new MavenProject();
        SebiMapperGeneratorMojo mojo = new SebiMapperGeneratorMojo( p );
        String[] compilerArguments = mojo.makeCompilerArguments( "src/test/java", "out" );
        assertThat( compilerArguments ).contains( "-cp",
                "src/test/java/entities/Course.java",
                "src/test/java/entities/Door.java",
                "src/test/java/entities/Engine.java",
                "src/test/java/entities/Person.java",
                "src/test/java/entities/Student.java",
                "src/test/java/entities/Tutor.java"
        );

//        fail( "method tCompilerArgs reached end. You know what to do." );
    }

}
