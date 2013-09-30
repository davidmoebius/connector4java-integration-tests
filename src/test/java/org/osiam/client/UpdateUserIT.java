package org.osiam.client;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osiam.client.connector.OsiamConnector;
import org.osiam.client.exception.ConflictException;
import org.osiam.client.oauth.GrantType;
import org.osiam.client.oauth.Scope;
import org.osiam.client.update.UpdateUser;
import org.osiam.resources.scim.Address;
import org.osiam.resources.scim.BasicMultiValuedAttribute;
import org.osiam.resources.scim.Email;
import org.osiam.resources.scim.Entitlement;
import org.osiam.resources.scim.Ims;
import org.osiam.resources.scim.Name;
import org.osiam.resources.scim.PhoneNumber;
import org.osiam.resources.scim.Photo;
import org.osiam.resources.scim.Role;
import org.osiam.resources.scim.User;
import org.osiam.resources.scim.GroupRef;
import org.osiam.resources.scim.X509Certificate;
import org.osiam.resources.type.EmailType;
import org.osiam.resources.type.GroupRefType;
import org.osiam.resources.type.ImsType;
import org.osiam.resources.type.PhoneNumberType;
import org.osiam.resources.type.PhotoType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/context.xml")
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class,
        DbUnitTestExecutionListener.class})
@DatabaseSetup("/database_seed.xml")
public class UpdateUserIT extends AbstractIntegrationTestBase {

    private String idExistingUser = "7d33bcbe-a54c-43d8-867e-f6146164941e";
    private UpdateUser updateUser;
    private User originalUser;
    private User returnUser;
    private User databaseUser;
    private static String IRRELEVANT = "Irrelevant";

    @Test
    @Ignore ("Ignored because of several bugs in the OSIAM server.")
    public void delete_multivalue_attributes(){
    	try{
	    	getOriginalUser("dma");
	        createUpdateUserWithMultiDeleteFields();
	        updateUser();
	        assertTrue(isValuePartOfMultivalueList(Email.class, originalUser.getEmails(), "hsimpson@atom-example.com"));
	        assertFalse(isValuePartOfMultivalueList(Email.class, returnUser.getEmails(), "hsimpson@atom-example.com"));
	        assertTrue(isValuePartOfMultivalueList(PhoneNumber.class, originalUser.getPhoneNumbers(), "0245817964"));
	        assertFalse(isValuePartOfMultivalueList(PhoneNumber.class, returnUser.getPhoneNumbers(), "0245817964"));
	        assertTrue(isValuePartOfMultivalueList(Ims.class, originalUser.getIms(), "ims01"));
	        assertFalse(isValuePartOfMultivalueList(Ims.class, returnUser.getIms(), "ims01"));
	        assertTrue(isValuePartOfMultivalueList(Photo.class, originalUser.getPhotos(), "photo01.jpg"));
	        assertFalse(isValuePartOfMultivalueList(Photo.class,returnUser.getPhotos(), "photo01.jpg"));
	        assertTrue(isValuePartOfMultivalueList(Role.class, originalUser.getRoles(), "role01"));
	        assertFalse(isValuePartOfMultivalueList(Role.class, returnUser.getRoles(), "role01"));
	        assertTrue(isValuePartOfAddressList(originalUser.getAddresses(), "formated address 01"));
	        assertFalse(isValuePartOfAddressList(returnUser.getAddresses(), "formated address 01"));//TODO other address was deleted
	        assertTrue(isValuePartOfMultivalueList(Entitlement.class, originalUser.getEntitlements(), "right2")); // TODO at the second run it will fail
	        assertFalse(isValuePartOfMultivalueList(Entitlement.class, returnUser.getEntitlements(), "right2"));//TODO at the second run it will fail
	        assertTrue(isValuePartOfMultivalueList(GroupRef.class, originalUser.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578"));//TODO Gruppen werden nicht gespeicher
	        assertFalse(isValuePartOfMultivalueList(GroupRef.class, returnUser.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578")); //TODO Gruppen werden nicht gespeicher
	        assertTrue(isValuePartOfMultivalueList(X509Certificate.class, originalUser.getX509Certificates(), "certificate01"));//TODO at the second run it will fail
	        assertFalse(isValuePartOfMultivalueList(X509Certificate.class, returnUser.getX509Certificates(), "certificate01"));//TODO at the second run it will fail
    	}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
    }

    @Ignore("Return user and database user not consistent yet. Check this always in updateUser() once consistency is reached and delete this test. Also should implement equals on User.")
    public void compare_returned_user_with_database_user() {
        try {
            // create test user
            getOriginalUser("dma");
            // create update user
            createUpdateUserWithMultiDeleteFields();
            // update test user with update user
            updateUser();
            // check consistency of update return value and user in database and expect equality
            assertTrue(returnUser.equals(databaseUser));
        } finally {
            oConnector.deleteUser(idExistingUser, accessToken);
        }
    }

    @Test
    @Ignore("Ignored due to single deletion is currently not working!")
    public void REGT_015_delete_multivalue_attributes_twice() {
        try {
            getOriginalUser("dma");
            createUpdateUserWithMultiDeleteFields();

            try {
                // try to delete twice
                updateUser();
                updateUser();
            } catch (Exception ex) {
                // should run without exception
                fail("Expected no exception, but got: " + ex.getMessage());
            }

            // entitlements and certificates available in the returned user should be deleted, even in the database
            assertTrue(isValuePartOfMultivalueList(Entitlement.class, originalUser.getEntitlements(), "right2"));
            assertFalse(isValuePartOfMultivalueList(Entitlement.class, returnUser.getEntitlements(), "right2"));
            assertFalse(isValuePartOfMultivalueList(Entitlement.class, databaseUser.getEntitlements(), "right2"));

            assertTrue(isValuePartOfMultivalueList(X509Certificate.class, originalUser.getX509Certificates(), "certificate01"));
            assertFalse(isValuePartOfMultivalueList(X509Certificate.class, returnUser.getX509Certificates(), "certificate01"));
            assertFalse(isValuePartOfMultivalueList(X509Certificate.class, databaseUser.getX509Certificates(), "certificate01"));

        } finally {
            oConnector.deleteUser(originalUser.getId(), accessToken);
        }
    }

    @Test
    @Ignore("Only ok because see TODO")
    public void delete_all_multivalue_attributes() {
        try {
            getOriginalUser("dama");
            createUpdateUserWithMultiAllDeleteFields();
            updateUser();
            assertNotNull(originalUser.getEmails());
            assertNull(returnUser.getEmails());
            assertNull(returnUser.getAddresses());
            assertNull(returnUser.getEntitlements());
            assertNull(returnUser.getGroups());//TODO da Gruppen nicht gespeichert werden sind sie immer null
            assertNull(returnUser.getIms());
            assertNull(returnUser.getPhoneNumbers());
            assertNull(returnUser.getPhotos());
            assertNull(returnUser.getRoles());
            assertNull(returnUser.getX509Certificates());
        } finally {
            oConnector.deleteUser(idExistingUser, accessToken);
        }
    }

    @Test
    @Ignore ("see TODO's")
    public void add_multivalue_attributes(){
    	try{
	    	getOriginalUser("ama");
	    	createUpdateUserWithMultiAddFields();
	    	updateUser();
	    	assertEquals(originalUser.getPhoneNumbers().size() + 1, returnUser.getPhoneNumbers().size());
	    	assertTrue(isValuePartOfMultivalueList(PhoneNumber.class, returnUser.getPhoneNumbers(), "99999999991"));
	    	assertEquals(originalUser.getEmails().size() + 1, returnUser.getEmails().size());//TODO funktioniert nicht. Eine mailadresse wird von server gelöscht
	    	assertTrue(isValuePartOfMultivalueList(Email.class, returnUser.getEmails(), "mac@muster.de"));
	    	assertEquals(originalUser.getAddresses().size() + 1, returnUser.getAddresses().size());//TODO neue Addresse löscht zuerst die alten
	    	getAddress(returnUser.getAddresses(), "new Address");
	    	assertEquals(originalUser.getEntitlements().size() + 1, returnUser.getEntitlements().size());//TODO at the second run it will fail
	    	assertTrue(isValuePartOfMultivalueList(Entitlement.class, returnUser.getEntitlements(), "right3"));//TODO at the second run it will fail
	    	assertEquals(originalUser.getGroups().size() + 1, returnUser.getGroups().size());//TODO gruppen werden aktuell nicht gespeichert
	    	assertTrue(isValuePartOfMultivalueList(GroupRef.class, returnUser.getGroups(), "d30a77eb-d7cf-4cd1-9fb3-cc640ef09578"));//TODO gruppen werden aktuell nicht gespeichert
	    	assertEquals(originalUser.getIms().size() + 1, returnUser.getIms().size());
	    	assertTrue(isValuePartOfMultivalueList(Ims.class, returnUser.getIms(), "ims03"));//TODO der type wird nicht geändert
	    	assertEquals(originalUser.getPhotos().size() + 1, returnUser.getPhotos().size());
	    	assertTrue(isValuePartOfMultivalueList(Photo.class, returnUser.getPhotos(), "photo03.jpg"));
	    	assertEquals(originalUser.getRoles().size() + 1, returnUser.getRoles().size());
	    	assertTrue(isValuePartOfMultivalueList(Role.class, returnUser.getRoles(), "role03"));
	    	assertEquals(originalUser.getX509Certificates().size() + 1, returnUser.getX509Certificates().size());//TODO at the second run it will fail
	    	assertTrue(isValuePartOfMultivalueList(X509Certificate.class, returnUser.getX509Certificates(), "certificate03"));//TODO at the second run it will fail
    	}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
    }

    @Test
    @Ignore ("see TODO's")
    public void update_multivalue_attributes(){
    	try{
	    	getOriginalUser("uma");
	    	createUpdateUserWithMultiUpdateFields();
	    	updateUser();
	    	//phonenumber
	    	PhoneNumber phoneNumber = getSingleMultiValueAttribute(PhoneNumber.class, returnUser.getPhoneNumbers(), "+497845/1157");
	    	assertFalse(phoneNumber.isPrimary());//TODO primary wird beim telefon nicht gesetzt
	    	phoneNumber = getSingleMultiValueAttribute(PhoneNumber.class, returnUser.getPhoneNumbers(), "0245817964");
	    	assertTrue(phoneNumber.isPrimary());//TODO primary wird beim telefon nicht gesetzt
	    	assertEquals(PhoneNumberType.OTHER, phoneNumber.getType());//TODO der type wird nicht geändert
	    	//email
	    	Email email = getSingleMultiValueAttribute(Email.class, returnUser.getEmails(), "hsimpson@atom-example.com");
	    	assertFalse(email.isPrimary());//TODO die atomadresse wird gelöscht und die andere wird nicht abgedatet
	    	email = getSingleMultiValueAttribute(Email.class, returnUser.getEmails(), "hsimpson@home-example.com");
	    	assertTrue(email.isPrimary());
	    	assertEquals(EmailType.OTHER, email.getType());
	    	Ims ims = getSingleMultiValueAttribute(Ims.class, returnUser.getIms(), "ims01");
	    	assertEquals(ImsType.ICQ, ims.getType());//TODO der type wird nicht upgedatet
	    	Photo photo = getSingleMultiValueAttribute(Photo.class, returnUser.getPhotos(), "photo01.jpg");
	    	assertEquals(PhotoType.PHOTO, photo.getType());//TODO der type wird nicht upgedatet
	    	GroupRef userGroup = getSingleMultiValueAttribute(GroupRef.class, returnUser.getGroups(), "69e1a5dc-89be-4343-976c-b5541af249f4"); //TODO gruppen werden nicht angelegt
	    	assertEquals(GroupRefType.INDIRECT, userGroup.getType());
    	}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
    }
    
	@Test
    public void update_all_single_values(){
		try{
	        getOriginalUser("uasv");
	        createUpdateUserWithUpdateFields();
	        updateUser();
	        assertEquals("UserName", returnUser.getUserName());
	        assertEquals("NickName", returnUser.getNickName());
	        assertNotEquals(originalUser.isActive(), returnUser.isActive());
	        assertEquals("DisplayName", returnUser.getDisplayName());
	        assertEquals("ExternalId", returnUser.getExternalId());
	        assertEquals("Locale", returnUser.getLocale());
	        assertEquals("PreferredLanguage", returnUser.getPreferredLanguage());
	        assertEquals("ProfileUrl", returnUser.getProfileUrl());
	        assertEquals("Timezone", returnUser.getTimezone());
	        assertEquals("Title", returnUser.getTitle());
	        assertEquals("UserType", returnUser.getUserType());
	        assertEquals("FamilyName", returnUser.getName().getFamilyName());
	        assertEquals("ExternalId", returnUser.getExternalId());
		}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
    }
	
	@Test
    public void delete_all_single_values(){
		try{
			getOriginalUser("desv");
	        createUpdateUserWithDeleteFields();
	        updateUser();
	        assertNull(returnUser.getNickName());
	        assertNull(returnUser.getDisplayName());
	        assertNull(returnUser.getLocale());
	        assertNull(returnUser.getPreferredLanguage());
	        assertNull(returnUser.getProfileUrl());
	        assertNull(returnUser.getTimezone());
	        assertNull(returnUser.getTitle());
	        assertNull(returnUser.getUserType());
	        assertNull(returnUser.getName());
	        assertNull(returnUser.getExternalId());
		}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
    }
	
	@Test
	public void update_password() {
		try{
			getOriginalUser("uasv");
	        createUpdateUserWithUpdateFields();
	        updateUser();
	        makeNewConnectionWithNewPassword();
		}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
	}
		
	@Test
	public void change_one_field_and_other_attributes_are_the_same(){
		try{
			getOriginalUser("cnaoaats");
			createUpdateUserWithJustOtherNickname();
			updateUser();
	        assertNotEquals(originalUser.getNickName(), returnUser.getNickName());
	        assertEquals(originalUser.isActive(), returnUser.isActive());
	        assertEquals(originalUser.getDisplayName(), returnUser.getDisplayName());
	        assertEquals(originalUser.getExternalId(), returnUser.getExternalId());
	        assertEquals(originalUser.getLocale(), returnUser.getLocale());
	        assertEquals(originalUser.getPreferredLanguage(), returnUser.getPreferredLanguage());
	        assertEquals(originalUser.getProfileUrl(), returnUser.getProfileUrl());
	        assertEquals(originalUser.getTimezone(), returnUser.getTimezone());
	        assertEquals(originalUser.getTitle(), returnUser.getTitle());
	        assertEquals(originalUser.getUserType(), returnUser.getUserType());
	        assertEquals(originalUser.getName().getFamilyName(), returnUser.getName().getFamilyName());
		}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
	}
	
	@Test (expected = ConflictException.class)
	@Ignore ("No exception is thrown an the moment")
	public void username_is_set_no_empty_string_is_thrown_probably(){
		try{
			getOriginalUser("ietiuuitp");
			createUpdateUserWithEmptyUserName();
			updateUser();
			fail("exception expected");
		}finally{
    		oConnector.deleteUser(idExistingUser, accessToken);
    	}
	}

	
	public <T extends BasicMultiValuedAttribute> boolean isValuePartOfMultivalueList(Class<T> clazz, List<T> list, String value){
		if(list != null){
			for (Object actAttribute : list) {
				BasicMultiValuedAttribute real = clazz.cast(actAttribute);
				if(real.getValue().equals(value)){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isValuePartOfAddressList(List<Address> list, String formated){
		if(list != null){
			for (Address actAttribute : list) {
				if(actAttribute.getFormatted().equals(formated)){
					return true;
				}
			}
		}
		return false;
	}
	
	public <T extends BasicMultiValuedAttribute> T getSingleMultiValueAttribute(Class<T> clazz, List<T> multiValues, Object value){
		if(multiValues != null){
			for (Object actMultiValuedAttribute : multiValues) {
				BasicMultiValuedAttribute real = clazz.cast(actMultiValuedAttribute);
				if(real.getValue().equals(value)){
					return (T) real;
				}
			}
		}
		fail("The value " + value + " could not be found");
		return null; //Can't be reached
	}
	
    public void delete_multivalue_attributes_which_is_not_available() {
        try {
            getOriginalUser("dma");
            createUpdateUserWithWrongEmail();
            updateUser();
        } finally {
            oConnector.deleteUser(idExistingUser, accessToken);
        }
    }






    @Test
    @Ignore("Ignored due to update user problem.")
    public void REGT_015_update_multivalue_attributes_twice() throws InterruptedException {
        try {
            getOriginalUser("uma");

            Thread.sleep(1000); // wait to grant different modification datetime

            createUpdateUserWithMultiUpdateFields();

            try {
                // try to update twice
                updateUser();
                updateUser();
            } catch (Exception ex) {
                // should run without exception
                fail("Expected no exception, but got: " + ex.getMessage());
            }

            // entitlements and certificates available in the update user should be updated
            Entitlement entitlementBefore = getSingleMultiValueAttribute(Entitlement.class, originalUser.getEntitlements(), "right1");
            Entitlement entitlementAfter = getSingleMultiValueAttribute(Entitlement.class, returnUser.getEntitlements(), "right1");
            assertEquals(entitlementBefore.getValue(), entitlementAfter.getValue());

            X509Certificate certificateBefore = getSingleMultiValueAttribute(X509Certificate.class, originalUser.getX509Certificates(), "certificate01");
            X509Certificate certificateAfter = getSingleMultiValueAttribute(X509Certificate.class, returnUser.getX509Certificates(), "certificate01");
            assertEquals(certificateBefore.getValue(), certificateAfter.getValue());

            assertNotEquals(originalUser.getMeta().getLastModified(), returnUser.getMeta().getLastModified());
        } finally {
            oConnector.deleteUser(idExistingUser, accessToken);
        }
    }

    @Test
    public void update_attributes_doesnt_change_the_password() {
        try {
            getOriginalUser("uadctp");
            createUpdateUserWithUpdateFieldsWithoutPassword();
            updateUser();
            makeNewConnection();
        } finally {
            oConnector.deleteUser(idExistingUser, accessToken);
        }
    }

    public void getOriginalUser(String userName) {
        User.Builder userBuilder = new User.Builder(userName);

        Email email01 = new Email.Builder().setValue("hsimpson@atom-example.com").setType(EmailType.WORK).setPrimary(true).build();
        Email email02 = new Email.Builder().setValue("hsimpson@home-example.com").setType(EmailType.WORK).build();
        List<Email> emails = new ArrayList<>();
        emails.add(email01);
        emails.add(email02);

        PhoneNumber phoneNumber01 = new PhoneNumber.Builder().setValue("+497845/1157").setType(PhoneNumberType.WORK).setPrimary(true).build();
        PhoneNumber phoneNumber02 = new PhoneNumber.Builder().setValue("0245817964").setType(PhoneNumberType.HOME).build();
        List<PhoneNumber> phoneNumbers = new ArrayList<>();
        phoneNumbers.add(phoneNumber01);
        phoneNumbers.add(phoneNumber02);

        Address simpleAddress01 = new Address.Builder().setCountry("de").setFormatted("formated address 01").setLocality("Berlin").setPostalCode("111111").build();
        Address simpleAddress02 = new Address.Builder().setCountry("en").setFormatted("address formated 02").setLocality("New York").setPostalCode("123456").build();
        List<Address> addresses = new ArrayList<>();
        addresses.add(simpleAddress01);
        addresses.add(simpleAddress02);   
        
        Entitlement entitlement01 = new Entitlement.Builder().setValue("right1").build();
        Entitlement entitlement02 = new Entitlement.Builder().setValue("right2").build();
        List<Entitlement> entitlements = new ArrayList<>();
        entitlements.add(entitlement01);
        entitlements.add(entitlement02);
        
        GroupRef group01 = new GroupRef.Builder().setValue("69e1a5dc-89be-4343-976c-b5541af249f4").setType(GroupRefType.DIRECT).build();
        GroupRef group02 = new GroupRef.Builder().setValue("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578").build();
        List<GroupRef> groups = new ArrayList<>();
        groups.add(group01);
        groups.add(group02);
        
        Ims ims01 = new Ims.Builder().setValue("ims01").setType(ImsType.SKYPE).build();
        Ims ims02 = new Ims.Builder().setValue("ims02").build();
        List<Ims> ims = new ArrayList<>();
        ims.add(ims01);
        ims.add(ims02);
        
        Photo photo01 = new Photo.Builder().setValue("photo01.jpg").setType(PhotoType.THUMBNAIL).build();
        Photo photo02 = new Photo.Builder().setValue("photo02.jpg").build();
        List<Photo> photos = new ArrayList<>();
        photos.add(photo01);
        photos.add(photo02);
        
        
        Role role01 = new Role.Builder().setValue("role01").build();
        Role role02 = new Role.Builder().setValue("role02").build();
        List<Role> roles = new ArrayList<>();
        roles.add(role01);
        roles.add(role02);
        
        X509Certificate certificate01 = new X509Certificate.Builder().setValue("certificate01").build();
        X509Certificate certificate02 = new X509Certificate.Builder().setValue("certificate02").build();
        List<X509Certificate> certificates = new ArrayList<>();
        certificates.add(certificate01);
        certificates.add(certificate02);
        
        Name name = new Name.Builder().setFamilyName("familiyName").setFormatted("formatted Name").setGivenName("givenName").build();

        userBuilder.setNickName("irgendwas")
                .setEmails(emails)
                .setPhoneNumbers(phoneNumbers)
                .setActive(false)
                .setDisplayName("irgendwas")
                .setLocale("de")
                .setPassword("geheim")
                .setPreferredLanguage("de")
                .setProfileUrl("irgendwas")
                .setTimezone("irgendwas")
                .setTitle("irgendwas")
                .setUserType("irgendwas")
                .setAddresses(addresses)
                .setGroups(groups)
                .setIms(ims)
                .setPhotos(photos)
                .setRoles(roles)
                .setName(name)
                .setX509Certificates(certificates)
                .setEntitlements(entitlements)
                .setExternalId("irgendwas")
        ;
        User newUser = userBuilder.build();

        originalUser = oConnector.createUser(newUser, accessToken);
        idExistingUser = originalUser.getId();
    }

    private void createUpdateUserWithUpdateFields() {
        Name newName = new Name.Builder().setFamilyName("FamilyName").build();
        updateUser = new UpdateUser.Builder()
                .updateUserName("UserName")
                .updateNickName("NickName")
                .updateExternalId("ExternalId")
                .updateDisplayName("DisplayName")
                .updatePassword("Password")
                .updateLocale("Locale")
                .updatePreferredLanguage("PreferredLanguage")
                .updateProfileUrl("ProfileUrl")
                .updateTimezone("Timezone")
                .updateTitle("Title")
                .updateUserType("UserType")
                .updateExternalId("ExternalId")
                .updateName(newName)
                .updateActive(true).build();
    }

    private void createUpdateUserWithUpdateFieldsWithoutPassword() {
        Name newName = new Name.Builder().setFamilyName("newFamilyName").build();
        updateUser = new UpdateUser.Builder()

        					.updateUserName(UUID.randomUUID().toString())
        					.updateNickName(IRRELEVANT)
        					.updateExternalId(IRRELEVANT)
        					.updateDisplayName(IRRELEVANT)
        					.updateLocale(IRRELEVANT)
        					.updatePreferredLanguage(IRRELEVANT)
        					.updateProfileUrl(IRRELEVANT)
        					.updateTimezone(IRRELEVANT)
        					.updateTitle(IRRELEVANT)
        					.updateUserType(IRRELEVANT)
        					.updateName(newName)
        					.updateActive(true).build();
    }
    
    private void createUpdateUserWithMultiUpdateFields(){
    	Email email = new Email.Builder()
    					.setValue("hsimpson@home-example.com").setType(EmailType.OTHER).setPrimary(true).build();
    	PhoneNumber phoneNumber = new PhoneNumber.Builder().setValue("0245817964").setType(PhoneNumberType.OTHER)
    			.setPrimary(true).build(); //Now the other should not be primary anymore
    	Ims ims = new Ims.Builder().setValue("ims01").setType(ImsType.ICQ).build();
    	Photo photo = new Photo.Builder().setValue("photo01.jpg").setType(PhotoType.PHOTO).build(); 
    	Role role = new Role.Builder().setValue("role01").build();
    	GroupRef group = new GroupRef.Builder().setValue("69e1a5dc-89be-4343-976c-b5541af249f4").setType(GroupRefType.INDIRECT).build();
    	
    	updateUser = new UpdateUser.Builder()
        					.addOrUpdateEmail(email)
        					.addOrUpdatesPhoneNumber(phoneNumber)
        					.addOrUpdatesPhoto(photo)
        					.addOrUpdatesIms(ims)
        					.addOrUpdateRole(role)
        					.addOrUpdateGroupMembership(group)
        					.build();
    }
    
    private void createUpdateUserWithDeleteFields(){
        updateUser = new UpdateUser.Builder() 
        					.deleteDisplayName()
        					.deleteNickName()
        					.deleteLocal()
        					.deletePreferredLanguage()
        					.deleteProfileUrl()
        					.deleteTimezone()
        					.deleteTitle()
        					.deleteUserType()
        					.deleteName()
        					.deleteExternalId()
        					.build();
    }
    
    private void createUpdateUserWithMultiDeleteFields(){

    	Address deleteAddress = null;
    	for (Address actAddress : originalUser.getAddresses()) {
    		if(actAddress.getFormatted().equals("formated address 01")){
    			deleteAddress = actAddress;
    			break;
    		}
		}
    	
    	updateUser = new UpdateUser.Builder() 
        					.deleteEmail("hsimpson@atom-example.com")
        					.deleteEntitlement("right2")
        					.deleteGroup("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578")
        					.deleteIms("ims01")
        					.deletePhoneNumber("0245817964")
        					.deletePhoto("photo01.jpg")
        					.deleteRole("role01")
        					.deleteX509Certificate("certificate01")
        					.deleteAddress(deleteAddress)
        					.build();
    }
    
    private void createUpdateUserWithWrongEmail(){

    	updateUser = new UpdateUser.Builder() 
        					.deleteEmail("test@test.com")
        					.build();
    }
    
    private void createUpdateUserWithMultiAddFields(){

    	Email email = new Email.Builder()
    					.setValue("mac@muster.de").setType(EmailType.HOME).build();
    	
    	PhoneNumber phonenumber = new PhoneNumber.Builder()
		.setValue("99999999991").setType(PhoneNumberType.HOME).build();
    	
    	Address newSimpleAddress = new Address.Builder().setCountry("fr").setFormatted("new Address").setLocality("New City").setPostalCode("66666").build();
    	Entitlement entitlement = new Entitlement.Builder().setValue("right3").build();
    	Ims ims = new Ims.Builder().setValue("ims03").build();
    	Photo photo = new Photo.Builder().setValue("photo03.jpg").build();
    	Role role = new Role.Builder().setValue("role03").build();
    	X509Certificate certificate = new X509Certificate.Builder().setValue("certificate03").build();
    	GroupRef groupMembership = new GroupRef.Builder().setValue("d30a77eb-d7cf-4cd1-9fb3-cc640ef09578").build();
    	
    	updateUser = new UpdateUser.Builder()
        					.addOrUpdateEmail(email)
        					.addOrUpdatesPhoneNumber(phonenumber)
        					.addAddress(newSimpleAddress)
        					.addOrUpdatesEntitlement(entitlement)
        					.addOrUpdateGroupMembership(groupMembership) //TODO Gruppen werden nicht gespeichert 
        					.addOrUpdatesIms(ims)
        					.addOrUpdatesPhoto(photo)
        					.addOrUpdateRole(role)
        					.addOrUpdateX509Certificate(certificate)//TODO at the second run it will fail
        					.build();
    }
    

    
    private void createUpdateUserWithMultiAllDeleteFields(){

    	updateUser = new UpdateUser.Builder()
        					.deleteEmails()
        					.deleteAddresses()
        					.deleteEntitlements()
        					.deleteGroups() //TODO Gruppen werden nicht gespeichert und können somit auch nicht gelöscht werden
        					.deleteIms()
        					.deletePhoneNumbers()
        					.deletePhotos()
        					.deleteRoles()
        					.deleteX509Certificates()
        					.build();
    }

    private void createUpdateUserWithJustOtherNickname() {
        updateUser = new UpdateUser.Builder()
                .updateNickName(IRRELEVANT)
                .build();
    }

    private void createUpdateUserWithEmptyUserName() {
        updateUser = new UpdateUser.Builder().updateUserName("")
                .build();
    }

    private void updateUser() {
        returnUser = oConnector.updateUser(idExistingUser, updateUser, accessToken);
        // also get user again from database to be able to compare with return object
        databaseUser = oConnector.getUser(returnUser.getId(), accessToken);
        /*
        TODO: Uncomment once returnUser and databaseUser are consistent!
         */
        // assertTrue(returnUser.equals(databaseUser));
    }

    private void makeNewConnectionWithNewPassword() {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("UserName").
                setPassword("Password").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
    }

    private void makeNewConnection() {
        OsiamConnector.Builder oConBuilder = new OsiamConnector.Builder(ENDPOINT_ADDRESS).
                setClientId(CLIENT_ID).
                setClientSecret(CLIENT_SECRET).
                setGrantType(GrantType.RESOURCE_OWNER_PASSWORD_CREDENTIALS).
                setUserName("marissa").
                setPassword("koala").
                setScope(Scope.ALL);
        oConnector = oConBuilder.build();
        oConnector.retrieveAccessToken();
    }

    public Address getAddress(List<Address> addresses, String formated) {
        Address returnAddress = null;
        if (addresses != null) {
            for (Address actAddress : addresses) {
                if (actAddress.getFormatted().equals(formated)) {
                    returnAddress = actAddress;
                    break;
                }
            }
        }
        if (returnAddress == null) {
            fail("The address with the formated part of " + formated + " could not be found");
        }
        return returnAddress;
    }

}
