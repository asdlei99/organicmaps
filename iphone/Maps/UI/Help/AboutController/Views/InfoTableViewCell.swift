final class InfoTableViewCell: UITableViewCell {

  override init(style: UITableViewCell.CellStyle, reuseIdentifier: String?) {
    super.init(style: .default, reuseIdentifier: reuseIdentifier)
    setupView()
  }
  required init?(coder: NSCoder) {
    super.init(coder: coder)
    setupView()
  }

  private func setupView() {
    backgroundView = UIView() // Set background color to clear
    textLabel?.setStyleAndApply("regular16:blackPrimaryText")
    textLabel?.numberOfLines = 0
    textLabel?.lineBreakMode = .byWordWrapping
    setStyleAndApply("ClearBackground")
  }

  func set(title: String, image: UIImage?) {
    textLabel?.text = title
    imageView?.image = image
  }
}
