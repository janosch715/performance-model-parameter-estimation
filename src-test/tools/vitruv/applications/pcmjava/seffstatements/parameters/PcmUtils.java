package tools.vitruv.applications.pcmjava.seffstatements.parameters;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.palladiosimulator.pcm.PcmPackage;
import org.palladiosimulator.pcm.repository.Repository;

public class PcmUtils {
	public static void saveModel(String filePath, Repository repository) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		URI filePathUri = URI.createFileURI(filePath);
		Resource resource = resourceSet.createResource(filePathUri);
		resource.getContents().add(repository);
		try {
			resource.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Repository loadModel(String filePath) {
		// Initialize package.
		PcmPackage.eINSTANCE.eClass();
		
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(
				Resource.Factory.Registry.DEFAULT_EXTENSION,
				new XMIResourceFactoryImpl());

		URI filePathUri = org.eclipse.emf.common.util.URI.createFileURI(filePath);
		
		Resource resource = resourceSet.getResource(filePathUri, true);
        return (Repository) resource.getContents().get(0);
	}
}
