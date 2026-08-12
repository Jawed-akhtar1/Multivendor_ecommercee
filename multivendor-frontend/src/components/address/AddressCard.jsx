const AddressCard = ({ address, onEdit, onDelete, isDeleting }) => {
  return (
    <article>
      <h3>{address.fullName}</h3>

      {address.isDefault && <span>Default Address</span>}

      <p>{address.phone}</p>

      <p>{address.addressLine}</p>

      {address.landmark && <p>Landmark: {address.landmark}</p>}

      <p>
        {address.city}, {address.state}
      </p>

      <p>
        {address.pincode}, {address.country}
      </p>

      <div>
        <button type="button" onClick={() => onEdit(address)}>
          Edit
        </button>

        <button
          type="button"
          onClick={() => onDelete(address.id)}
          disabled={isDeleting}
        >
          {isDeleting ? "Deleting..." : "Delete"}
        </button>
      </div>
    </article>
  );
};

export default AddressCard;
