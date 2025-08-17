declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    message?: string
    data?: boolean
  }

  type BaseResponseLong = {
    code?: number
    message?: string
    data?: number
  }

  type BaseResponsePageUserVo = {
    code?: number
    message?: string
    data?: PageUserVo
  }

  type BaseResponseString = {
    code?: number
    message?: string
    data?: string
  }

  type BaseResponseUser = {
    code?: number
    message?: string
    data?: User
  }

  type BaseResponseUserVo = {
    code?: number
    message?: string
    data?: UserVo
  }

  type DeleteRequest = {
    id?: number
  }

  type getUserByIdParams = {
    id: number
  }

  type getUserVoByIdParams = {
    id: number
  }

  type PageUserVo = {
    records?: UserVo[]
    pageNumber?: number
    pageSize?: number
    totalPage?: number
    totalRow?: number
    optimizeCountQuery?: boolean
  }

  type User = {
    id?: number
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: number
  }

  type UserAddReq = {
    userName?: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginReq = {
    userAccount?: string
    userPassword?: string
  }

  type UserQueryReq = {
    current?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: number
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterReq = {
    userAccount?: string
    userPassword?: string
    confirmPassword?: string
  }

  type UserUpdateReq = {
    id?: number
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserUpdateSelfReq = {
    userName?: string
    userAvatar?: string
    userProfile?: string
    oldPassword?: string
    newPassword?: string
    confirmPassword?: string
  }

  type UserVo = {
    id?: number
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
