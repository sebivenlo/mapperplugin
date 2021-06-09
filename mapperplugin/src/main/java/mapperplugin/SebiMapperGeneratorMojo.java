package mapperplugin;

import genericmapper.GetterNamingStrategy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author Pieter van den Hombergh {@code Pieter.van.den.Hombergh@gmail.com}
 */
@Mojo( name = "sebimappergenerator",
        defaultPhase = LifecyclePhase.GENERATE_SOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        requiresProject = true,
        threadSafe = false )
/**
 * @goal generate
 * @phase process-classes
 * @configurator include-project-dependencies
 * @requiresDependencyResolution compile+runtime
 */
public class SebiMapperGeneratorMojo extends AbstractMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        String baseDir = project.getBasedir().toString();
        try {
            makeTargetDirs();
            compileSources();
//            System.out.println( "outDir = " + outDir );
            var runner = new GeneratorRunner( classesDir, outDir, entityPackages,
                    getDependencyJars() );
            runner.run();
//            compileGenerated();
        } catch ( IOException | DependencyResolutionRequiredException
                | InterruptedException ex ) {
            Logger.getLogger( SebiMapperGeneratorMojo.class.getName() )
                    .log( Level.SEVERE, null, ex );
        }
    }

    @Parameter( property = "mapper.entities.packages", defaultValue = "false" )
    protected List<String> entityPackages;

    @Parameter( property = "mapper.generator.outDir", defaultValue = "false" )
    protected String outDir;

    @Parameter( property = "mapper.generator.classesDir", defaultValue = "false" )
    protected String classesDir;

    @Parameter( property = "mapper.generator.classesDir", defaultValue = "BEAN" )
    protected GetterNamingStrategy getterNameStrategy;

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    void makeTargetDirs() throws IOException {
        List<String> elements
                = List.of( "target" + fileSep
                        + "classes", "target" + fileSep
                        + "/test-classes" );
        for ( String element : elements ) {
            Path p = Path.of( project.getBasedir() + fileSep + element );
            Files.createDirectories( p );
        }
    }

    int compileSources() throws IOException,
            DependencyResolutionRequiredException {
        if ( classesDir.endsWith( "test-classes" ) ) {
            return runCompiler( project.getBuild().getTestSourceDirectory(),
                    project.getBuild().getTestOutputDirectory() );
        }
        return runCompiler( project.getBuild().getSourceDirectory(),
                project.getBuild().getOutputDirectory() );
    }

    int runCompiler( String sources, String outDir ) throws
            DependencyResolutionRequiredException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        String[] compilerArguments = makeCompilerArguments( sources, outDir );
        return compiler.run( null, null, null, compilerArguments );
    }

    String[] makeCompilerArguments( String sourceDir, String targetDir ) throws
            DependencyResolutionRequiredException {
        String[] sources = getSourceFiles( sourceDir );
        String compileClassPath = project
                .getCompileClasspathElements()
                .stream()
                .collect( joining( pathSep ) );
        String[] opts = { "-p", compileClassPath, "-cp", compileClassPath, "-d", targetDir };
        String[] allOpts = Arrays.copyOf( opts, opts.length + sources.length );
        System.arraycopy( sources, 0, allOpts, opts.length, sources.length );
        return allOpts;
    }

    public String[] getSourceFiles( String startDir ) {
        String[] result = null;
        try ( Stream<Path> stream = Files.walk( Paths.get( startDir ),
                Integer.MAX_VALUE ) ) {
            result = stream
                    .filter( file -> !Files.isDirectory( file ) )
                    .filter( file -> file.getFileName().toString().endsWith(
                    ".java" ) )
                    .map( Path::toString )
                    .toArray( String[]::new );
        } catch ( IOException ignored ) {
        }
        return result;
    }
    static String pathSep = System.getProperty( "path.separator" );
    static String fileSep = System.getProperty( "file.separator" );

    String getDependencyJars() {
        List<Dependency> dependencies = project.getDependencies();
        String repo = System.getProperty( "user.home" )
                + fileSep + ".m2" + fileSep + "repository";
        return dependencies.stream()
                .map( ( Dependency d ) -> {
                    String gid = d.getGroupId().replace( ".", fileSep );
                    String artifactId = d.getArtifactId();
                    String version = d.getVersion();
                    var jar = artifactId + "-" + version + ".jar";
                    return String.join( fileSep, repo, gid,
                            artifactId, version, jar );

                } )
                .collect( joining( pathSep ) );
    }
}
