/*******************************************************************************
 * Copyright (c) 2014 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Initial API and implementation and/or initial documentation - Jay Jay Billings,
 *   Jordan H. Deyton, Dasha Gorin, Alexander J. McCaskey, Taylor Patterson,
 *   Claire Saunders, Matthew Wang, Anna Wojtowicz
 *******************************************************************************/
package org.eclipse.ice.proteus;

import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.ice.datastructures.form.FormStatus;
import org.eclipse.ice.item.Item;
import org.eclipse.ice.item.jobLauncher.JobLauncher;
import org.eclipse.core.resources.IProject;

/**
 * <!-- begin-UML-doc -->
 * <p>
 * A PROTEUS Item for launching jobs.
 * </p>
 * <!-- end-UML-doc -->
 * 
 * @author Jay Jay Billings, w5q
 * @generated 
 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
 */
@XmlRootElement(name = "PROTEUSLauncher")
public class PROTEUSLauncher extends JobLauncher {

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The constructor.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public PROTEUSLauncher() {
		// begin-user-code
		// Forward
		this(null);
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * The constructor with a project space in which files should be
	 * manipulated.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @param projectSpace
	 *            <p>
	 *            The Eclipse project where files should be stored and from
	 *            which they should be retrieved.
	 *            </p>
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public PROTEUSLauncher(IProject projectSpace) {
		// begin-user-code
		// Forward up
		super(projectSpace);
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation launches the PROTEUS job.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @param actionName
	 *            <p>
	 *            The name of action that should be performed using the
	 *            processed Form data.
	 *            </p>
	 * @return <p>
	 *         The status of the Item after processing the Form and executing
	 *         the action. It returns FormStatus.InfoError if it is unable to
	 *         run for any reason, including being asked to run actions that are
	 *         not in the list of available actions.
	 *         </p>
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	public FormStatus process(String actionName) {
		// begin-user-code
		// Just forward the request to the super class. This operation is only
		// here in case we need to do some work before processing.
		return super.process(actionName);
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation sets up the form for the PROTEUSLauncher.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	protected void setupForm() {
		// begin-user-code

		// Local Declarations
		String cpDataStep = "cp ${installDir}../../../../../install/sharp_examples/sahex1/sahex1_unic* .;";
		String relinkStep = "ln -s ${inputFile} sahex1_unic.inp;";
		String launchStep = "${installDir}sn2nd.x -input sahex1_unic.inp;";
		String allLaunchSteps = cpDataStep + relinkStep + launchStep;
		String localInstallDir = "sharp/trunk/modules/unic/src/bin";
		String remoteInstallDir = "/home/nfs/w5q/sharp/defaultBuild/modules/unic/src/bin";

		// Setup the Form
		super.setupForm();

		// Setup the executable information
		setExecutable(getName(), getDescription(), allLaunchSteps);

		// Add a couple of hosts
		addHost("localhost.localdomain","linux x86_64", localInstallDir);
		addHost("ergaster.ornl.gov", "linux x86_64", remoteInstallDir);

		return;
		// end-user-code
	}

	/**
	 * <!-- begin-UML-doc -->
	 * <p>
	 * This operation is used to setup the name and description of the launcher.
	 * </p>
	 * <!-- end-UML-doc -->
	 * 
	 * @generated 
	 *            "UML to Java (com.ibm.xtools.transform.uml2.java5.internal.UML2JavaTransform)"
	 */
	protected void setupItemInfo() {
		// begin-user-code

		// Set the name and description of the Item
		setName(PROTEUSLauncherBuilder.name);
		setDescription("PROTEUS is a nuclear reactor core simulator from "
				+ "Argonne National Laboratory that is primarily focused "
				+ "on sodium-cooled fast reactors.");

		return;
		// end-user-code
	}
}